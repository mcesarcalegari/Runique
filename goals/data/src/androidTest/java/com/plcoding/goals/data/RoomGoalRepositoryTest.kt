package com.plcoding.goals.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.plcoding.core.database.RunDatabase
import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.Result
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RoomGoalRepositoryTest {

    private lateinit var db: RunDatabase
    private lateinit var runRepository: FakeRunRepository
    private lateinit var clock: MutableClock
    private lateinit var repository: RoomGoalRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RunDatabase::class.java
        ).allowMainThreadQueries().build()
        runRepository = FakeRunRepository()
        clock = MutableClock(local("2024-03-07T12:00"))
        repository = RoomGoalRepository(db.goalDao, runRepository, clock)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private suspend fun createDistanceGoal(km: Long = 30, period: GoalPeriod = GoalPeriod.WEEKLY): String {
        val result = repository.createGoal(GoalType.DISTANCE, period, km * 1_000)
        assertThat(result).isInstanceOf(Result.Success::class)
        return (result as Result.Success).data
    }

    @Test
    fun createGoal_storesCurrentCalendarPeriod() = runTest {
        val weekly = createDistanceGoal(period = GoalPeriod.WEEKLY)
        val monthly = createDistanceGoal(period = GoalPeriod.MONTHLY)

        val weeklyGoal = (repository.getGoal(weekly) as Result.Success).data
        val monthlyGoal = (repository.getGoal(monthly) as Result.Success).data
        assertThat(weeklyGoal.periodStart).isEqualTo(LocalDate.parse("2024-03-04"))
        assertThat(weeklyGoal.periodEnd).isEqualTo(LocalDate.parse("2024-03-10"))
        assertThat(monthlyGoal.periodStart).isEqualTo(LocalDate.parse("2024-03-01"))
        assertThat(monthlyGoal.periodEnd).isEqualTo(LocalDate.parse("2024-03-31"))
        assertThat(weeklyGoal.completedAt).isNull()
    }

    @Test
    fun observeGoals_reEmitsWithUpdatedProgress_whenRunsChange() = runTest {
        createDistanceGoal()

        repository.observeGoals().test {
            assertThat(awaitItem().single().progress).isEqualTo(0L)

            runRepository.add(run("r1", "2024-03-05T07:00", 18_400))
            val updated = awaitItem().single()
            assertThat(updated.progress).isEqualTo(18_400L)
            assertThat(updated.percent).isEqualTo(61L)
            assertThat(updated.status).isEqualTo(GoalStatus.ACTIVE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeGoals_flipsToMissed_whenClockPassesPeriodEnd() = runTest {
        createDistanceGoal()
        runRepository.add(run("r1", "2024-03-05T07:00", 22_500))
        clock.now = local("2024-03-10T23:59")

        repository.observeGoals().test {
            assertThat(awaitItem().single().status).isEqualTo(GoalStatus.ACTIVE)

            clock.now = local("2024-03-11T00:00")
            advanceTimeBy(60_001)
            val missed = awaitItem().single()
            assertThat(missed.status).isEqualTo(GoalStatus.MISSED)
            assertThat(missed.percent).isEqualTo(75L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createGoal_targetAlreadyReached_persistsCompletedAt() = runTest {
        runRepository.add(run("r1", "2024-03-05T07:00", 32_000))

        val id = createDistanceGoal()

        assertThat(db.goalDao.getById(id)?.completedAtUtc).isNotNull()
        repository.observeGoals().test {
            assertThat(awaitItem().single().status).isEqualTo(GoalStatus.COMPLETED)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateTarget_belowProgress_persistsCompletedAt() = runTest {
        runRepository.add(run("r1", "2024-03-05T07:00", 18_400))
        val id = createDistanceGoal(km = 40)
        assertThat(db.goalDao.getById(id)?.completedAtUtc).isNull()

        val result = repository.updateTarget(id, 15_000)

        assertThat(result).isEqualTo(Result.Success(Unit))
        val entity = db.goalDao.getById(id)
        assertThat(entity?.target).isEqualTo(15_000L)
        assertThat(entity?.completedAtUtc).isNotNull()
        repository.observeGoals().test {
            val tracked = awaitItem().single()
            assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
            assertThat(tracked.percent).isEqualTo(122L)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateTarget_keepsTypeAndPeriod() = runTest {
        val id = createDistanceGoal(km = 30)

        repository.updateTarget(id, 40_000)

        val goal = (repository.getGoal(id) as Result.Success).data
        assertThat(goal.target).isEqualTo(40_000L)
        assertThat(goal.type).isEqualTo(GoalType.DISTANCE)
        assertThat(goal.period).isEqualTo(GoalPeriod.WEEKLY)
        assertThat(goal.periodStart).isEqualTo(LocalDate.parse("2024-03-04"))
    }

    @Test
    fun updateTarget_unknownGoal_returnsNotFound() = runTest {
        assertThat(repository.updateTarget("missing", 1_000))
            .isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
        assertThat(repository.getGoal("missing")).isEqualTo(Result.Error(DataError.Local.NOT_FOUND))
    }

    @Test
    fun deleteGoal_removesGoal_andKeepsRuns() = runTest {
        runRepository.add(run("r1", "2024-03-05T07:00", 5_000))
        val id = createDistanceGoal()

        repository.deleteGoal(id)

        assertThat(db.goalDao.getById(id)).isNull()
        assertThat(runRepository.runs.value.size).isEqualTo(1)
    }

    @Test
    fun multipleGoals_withSameTypeAndPeriod_areAllListed() = runTest {
        createDistanceGoal()
        createDistanceGoal()

        repository.observeGoals().test {
            assertThat(awaitItem().size).isEqualTo(2)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
