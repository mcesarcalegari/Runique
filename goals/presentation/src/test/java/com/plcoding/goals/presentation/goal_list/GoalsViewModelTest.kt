package com.plcoding.goals.presentation.goal_list

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.FakeGoalRepository
import com.plcoding.goals.presentation.goal_list.model.GoalTimeLabel
import com.plcoding.goals.presentation.trackedGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class GoalsViewModelTest {

    private lateinit var repository: FakeGoalRepository
    private lateinit var viewModel: GoalsViewModel
    private lateinit var defaultLocale: Locale

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        repository = FakeGoalRepository()
        viewModel = GoalsViewModel(repository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `initial state is loading on the active tab`() {
        val state = viewModel.state.value
        assertThat(state.isLoading).isTrue()
        assertThat(state.selectedTab).isEqualTo(GoalsTab.ACTIVE)
    }

    @Test
    fun `first emission ends loading`() = runTest {
        viewModel.state.test {
            val loaded = awaitItem()
            assertThat(loaded.isLoading).isFalse()
            assertThat(loaded.visibleGoals).isEmpty()
        }
    }

    @Test
    fun `goals are split into tabs by status`() = runTest {
        repository.goals.value = listOf(
            trackedGoal(id = "a", status = GoalStatus.ACTIVE),
            trackedGoal(id = "c", status = GoalStatus.COMPLETED),
            trackedGoal(id = "m", status = GoalStatus.MISSED)
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.activeGoals.map { it.id }).containsExactly("a")
            assertThat(state.completedGoals.map { it.id }).containsExactly("c")
            assertThat(state.missedGoals.map { it.id }).containsExactly("m")
            assertThat(state.visibleGoals.map { it.id }).containsExactly("a")
        }
    }

    @Test
    fun `selecting a tab shows only its goals`() = runTest {
        repository.goals.value = listOf(
            trackedGoal(id = "a", status = GoalStatus.ACTIVE),
            trackedGoal(id = "m", status = GoalStatus.MISSED, daysLeft = -3)
        )

        viewModel.state.test {
            awaitItem()
            viewModel.onAction(GoalsAction.OnTabSelect(GoalsTab.MISSED))
            val state = awaitItem()
            assertThat(state.selectedTab).isEqualTo(GoalsTab.MISSED)
            assertThat(state.visibleGoals.map { it.id }).containsExactly("m")
        }
    }

    @Test
    fun `active goals are sorted by period end ascending, others descending`() = runTest {
        repository.goals.value = listOf(
            trackedGoal(id = "a-month", status = GoalStatus.ACTIVE, start = "2024-03-01", end = "2024-03-31"),
            trackedGoal(id = "a-week", status = GoalStatus.ACTIVE, start = "2024-03-04", end = "2024-03-10"),
            trackedGoal(id = "m-jan", status = GoalStatus.MISSED, start = "2024-01-01", end = "2024-01-31", daysLeft = -36),
            trackedGoal(id = "m-feb", status = GoalStatus.MISSED, start = "2024-02-19", end = "2024-02-25", daysLeft = -11),
            trackedGoal(id = "c-old", status = GoalStatus.COMPLETED, start = "2024-02-01", end = "2024-02-29", daysLeft = -7),
            trackedGoal(id = "c-new", status = GoalStatus.COMPLETED, start = "2024-02-26", end = "2024-03-03", daysLeft = -4)
        )

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.activeGoals.map { it.id }).containsExactly("a-week", "a-month")
            assertThat(state.missedGoals.map { it.id }).containsExactly("m-feb", "m-jan")
            assertThat(state.completedGoals.map { it.id }).containsExactly("c-new", "c-old")
        }
    }

    @Test
    fun `distance card is formatted with one decimal and the date range`() = runTest {
        repository.goals.value = listOf(trackedGoal(progress = 18_400, target = 30_000, daysLeft = 3))

        viewModel.state.test {
            val goal = awaitItem().activeGoals.single()
            assertThat(goal.progress).isEqualTo("18.4")
            assertThat(goal.target).isEqualTo("30")
            assertThat(goal.percent).isEqualTo(61L)
            assertThat(goal.dateRange).isEqualTo("Mar 4 – Mar 10")
            assertThat(goal.timeLabel).isEqualTo(GoalTimeLabel.DaysLeft(3))
            assertThat(goal.isEditable).isTrue()
        }
    }

    @Test
    fun `duration card is formatted in hours and minutes`() = runTest {
        repository.goals.value = listOf(
            trackedGoal(
                type = GoalType.DURATION,
                period = GoalPeriod.MONTHLY,
                target = 36_000,
                progress = 4 * 3_600L + 12 * 60 + 59,
                start = "2024-03-01",
                end = "2024-03-31"
            )
        )

        viewModel.state.test {
            val goal = awaitItem().activeGoals.single()
            assertThat(goal.progress).isEqualTo("4h 12m")
            assertThat(goal.target).isEqualTo("10")
            assertThat(goal.dateRange).isEqualTo("Mar 1 – Mar 31")
        }
    }

    @Test
    fun `frequency card shows run counts`() = runTest {
        repository.goals.value = listOf(trackedGoal(type = GoalType.FREQUENCY, target = 5, progress = 3))

        viewModel.state.test {
            val goal = awaitItem().activeGoals.single()
            assertThat(goal.progress).isEqualTo("3")
            assertThat(goal.target).isEqualTo("5")
            assertThat(goal.percent).isEqualTo(60L)
        }
    }

    @Test
    fun `distance progress keeps a trailing zero decimal`() = runTest {
        repository.goals.value = listOf(trackedGoal(target = 120_000, progress = 82_000))

        viewModel.state.test {
            assertThat(awaitItem().activeGoals.single().progress).isEqualTo("82.0")
        }
    }

    @Test
    fun `progress bar is clamped to full above target`() = runTest {
        repository.goals.value = listOf(
            trackedGoal(progress = 32_100, status = GoalStatus.COMPLETED, daysLeft = -4)
        )

        viewModel.state.test {
            val goal = awaitItem().completedGoals.single()
            assertThat(goal.percent).isEqualTo(107L)
            assertThat(goal.progressFraction).isEqualTo(1f)
            assertThat(goal.isEditable).isFalse()
        }
    }

    @Test
    fun `ended goals show the ended date and running completed goals show the end date`() = runTest {
        repository.goals.value = listOf(
            trackedGoal(id = "ended", status = GoalStatus.COMPLETED, start = "2024-02-26", end = "2024-03-03", daysLeft = -4),
            trackedGoal(id = "running", status = GoalStatus.COMPLETED, daysLeft = 3),
            trackedGoal(id = "missed", status = GoalStatus.MISSED, start = "2024-02-19", end = "2024-02-25", daysLeft = -11)
        )

        viewModel.state.test {
            val state = awaitItem()
            val byId = state.completedGoals.associateBy { it.id }
            assertThat(byId.getValue("ended").timeLabel).isEqualTo(GoalTimeLabel.Ended("Mar 3"))
            assertThat(byId.getValue("running").timeLabel).isEqualTo(GoalTimeLabel.Ends("Mar 10"))
            assertThat(state.missedGoals.single().timeLabel).isEqualTo(GoalTimeLabel.Ended("Feb 25"))
        }
    }

    @Test
    fun `goal moves tabs when the repository emits a new status`() = runTest {
        repository.goals.value = listOf(trackedGoal(id = "g", progress = 28_000))

        viewModel.state.test {
            assertThat(awaitItem().activeGoals.map { it.id }).containsExactly("g")

            repository.goals.value = listOf(
                trackedGoal(id = "g", progress = 31_000, status = GoalStatus.COMPLETED)
            )
            val state = awaitItem()
            assertThat(state.activeGoals).isEmpty()
            assertThat(state.completedGoals.single().percent).isEqualTo(103L)
        }
    }
}
