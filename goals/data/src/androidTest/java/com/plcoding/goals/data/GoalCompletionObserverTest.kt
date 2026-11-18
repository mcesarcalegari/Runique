package com.plcoding.goals.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.plcoding.core.database.RunDatabase
import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.goals.domain.GoalStatus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalCompletionObserverTest {

    private lateinit var db: RunDatabase
    private lateinit var runRepository: FakeRunRepository
    private lateinit var clock: MutableClock

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RunDatabase::class.java
        ).allowMainThreadQueries().build()
        runRepository = FakeRunRepository()
        clock = MutableClock(local("2024-03-07T12:00"))
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun weeklyGoal(id: String, start: String = "2024-03-04", end: String = "2024-03-10") = GoalEntity(
        type = "DISTANCE",
        period = "WEEKLY",
        target = 30_000,
        periodStartDate = start,
        periodEndDate = end,
        createdAtUtc = "2024-03-04T10:00:00Z",
        completedAtUtc = null,
        id = id
    )

    @Test
    fun runAddedThenDeleted_goalStaysCompleted() = runTest {
        db.goalDao.upsert(weeklyGoal("goal"))
        val job = GoalCompletionObserver(db.goalDao, runRepository, clock).start(backgroundScope)

        runRepository.add(run("r1", "2024-03-05T07:00", 31_000))
        db.goalDao.observeAll().filter { it.single().completedAtUtc != null }.test {
            assertThat(awaitItem().single().completedAtUtc).isNotNull()
            cancelAndIgnoreRemainingEvents()
        }

        runRepository.deleteRun("r1")

        val repository = RoomGoalRepository(db.goalDao, runRepository, clock)
        repository.observeGoals().test {
            val tracked = awaitItem().single()
            assertThat(tracked.progress).isEqualTo(0L)
            assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
            cancelAndIgnoreRemainingEvents()
        }
        job.cancel()
    }

    @Test
    fun goalWhosePeriodEnded_isNeverLatched() = runTest {
        db.goalDao.upsert(weeklyGoal("old", start = "2024-02-26", end = "2024-03-03"))
        val job = GoalCompletionObserver(db.goalDao, runRepository, clock).start(backgroundScope)

        runRepository.add(run("r1", "2024-03-01T07:00", 31_000))
        // A goal in the running period acts as a marker that the observer processed this emission.
        db.goalDao.upsert(weeklyGoal("marker"))
        db.goalDao.observeAll()
            .filter { goals -> goals.any { it.id == "marker" } }
            .test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }
        runRepository.add(run("r2", "2024-03-05T07:00", 31_000))
        db.goalDao.observeAll()
            .filter { goals -> goals.first { it.id == "marker" }.completedAtUtc != null }
            .test {
                awaitItem()
                cancelAndIgnoreRemainingEvents()
            }

        assertThat(db.goalDao.getById("old")?.completedAtUtc).isNull()
        job.cancel()
    }
}
