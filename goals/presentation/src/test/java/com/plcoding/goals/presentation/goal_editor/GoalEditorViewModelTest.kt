package com.plcoding.goals.presentation.goal_editor

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.plcoding.core.domain.util.DataError
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.FakeGoalRepository
import com.plcoding.goals.presentation.trackedGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GoalEditorViewModelTest {

    private lateinit var repository: FakeGoalRepository

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repository = FakeGoalRepository()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newGoalViewModel(handle: SavedStateHandle = SavedStateHandle()) =
        GoalEditorViewModel(repository, handle)

    private fun editViewModel(goalId: String = "existing") =
        GoalEditorViewModel(repository, SavedStateHandle(mapOf("goalId" to goalId)))

    private fun GoalEditorViewModel.type(text: String) =
        onAction(GoalEditorAction.OnTargetChange(text))

    // Defaults

    @Test
    fun `new goal defaults to distance, weekly, empty target and save disabled`() {
        val state = newGoalViewModel().state.value
        assertThat(state.isEditMode).isFalse()
        assertThat(state.type).isEqualTo(GoalType.DISTANCE)
        assertThat(state.period).isEqualTo(GoalPeriod.WEEKLY)
        assertThat(state.targetText).isEqualTo("")
        assertThat(state.canSave).isFalse()
    }

    // Save enabled / disabled

    @Test
    fun `save is disabled for zero and enabled for a positive target`() {
        val viewModel = newGoalViewModel()

        viewModel.type("0")
        assertThat(viewModel.state.value.canSave).isFalse()

        viewModel.type("12.5")
        assertThat(viewModel.state.value.canSave).isTrue()

        viewModel.type("")
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `input breaking the type rules is not accepted`() {
        val viewModel = newGoalViewModel()
        viewModel.type("12.5")

        viewModel.type("12.55")
        assertThat(viewModel.state.value.targetText).isEqualTo("12.5")

        viewModel.onAction(GoalEditorAction.OnTypeSelect(GoalType.FREQUENCY))
        viewModel.type("2")
        viewModel.type("2.5")
        assertThat(viewModel.state.value.targetText).isEqualTo("2")
        assertThat(viewModel.state.value.canSave).isTrue()
    }

    // Unit switching

    @Test
    fun `switching type keeps a target that is valid for the new type`() {
        val viewModel = newGoalViewModel()
        viewModel.type("30")

        viewModel.onAction(GoalEditorAction.OnTypeSelect(GoalType.FREQUENCY))

        assertThat(viewModel.state.value.type).isEqualTo(GoalType.FREQUENCY)
        assertThat(viewModel.state.value.targetText).isEqualTo("30")
    }

    @Test
    fun `switching type clears a decimal target the new type cannot hold`() {
        val viewModel = newGoalViewModel()
        viewModel.type("12.5")

        viewModel.onAction(GoalEditorAction.OnTypeSelect(GoalType.DURATION))

        assertThat(viewModel.state.value.targetText).isEqualTo("")
        assertThat(viewModel.state.value.canSave).isFalse()
    }

    @Test
    fun `selecting monthly changes the period`() {
        val viewModel = newGoalViewModel()
        viewModel.onAction(GoalEditorAction.OnPeriodSelect(GoalPeriod.MONTHLY))
        assertThat(viewModel.state.value.period).isEqualTo(GoalPeriod.MONTHLY)
    }

    // Create

    @Test
    fun `saving a new goal creates it in base units and emits saved`() = runTest {
        val viewModel = newGoalViewModel()
        viewModel.type("30")

        viewModel.events.test {
            viewModel.onAction(GoalEditorAction.OnSaveClick)
            assertThat(awaitItem()).isEqualTo(GoalEditorEvent.GoalSaved)
        }
        assertThat(repository.created).containsExactly(Triple(GoalType.DISTANCE, GoalPeriod.WEEKLY, 30_000L))
    }

    @Test
    fun `save click with an invalid target does nothing`() = runTest {
        val viewModel = newGoalViewModel()
        viewModel.onAction(GoalEditorAction.OnSaveClick)
        assertThat(repository.created).isEmpty()
    }

    @Test
    fun `failed save emits an error`() = runTest {
        repository.nextError = DataError.Local.DISK_FULL
        val viewModel = newGoalViewModel()
        viewModel.type("5")

        viewModel.events.test {
            viewModel.onAction(GoalEditorAction.OnSaveClick)
            assertThat(awaitItem()).isInstanceOf(GoalEditorEvent.Error::class)
        }
        assertThat(viewModel.state.value.isSaving).isFalse()
    }

    // Edit

    @Test
    fun `edit mode loads the goal with type and period locked`() {
        repository.goals.value = listOf(
            trackedGoal(id = "existing", type = GoalType.DURATION, period = GoalPeriod.MONTHLY, target = 36_000)
        )
        val viewModel = editViewModel()

        val state = viewModel.state.value
        assertThat(state.isEditMode).isTrue()
        assertThat(state.isLoadingGoal).isFalse()
        assertThat(state.type).isEqualTo(GoalType.DURATION)
        assertThat(state.period).isEqualTo(GoalPeriod.MONTHLY)
        assertThat(state.targetText).isEqualTo("10")
        assertThat(state.canSave).isTrue()

        viewModel.onAction(GoalEditorAction.OnTypeSelect(GoalType.DISTANCE))
        viewModel.onAction(GoalEditorAction.OnPeriodSelect(GoalPeriod.WEEKLY))
        assertThat(viewModel.state.value.type).isEqualTo(GoalType.DURATION)
        assertThat(viewModel.state.value.period).isEqualTo(GoalPeriod.MONTHLY)
    }

    @Test
    fun `edit mode with a missing goal emits an error`() = runTest {
        val viewModel = editViewModel(goalId = "missing")
        viewModel.events.test {
            assertThat(awaitItem()).isInstanceOf(GoalEditorEvent.Error::class)
        }
    }

    @Test
    fun `updating the target keeps type and period`() = runTest {
        repository.goals.value = listOf(trackedGoal(id = "existing", target = 30_000))
        val viewModel = editViewModel()
        viewModel.type("40")

        viewModel.events.test {
            viewModel.onAction(GoalEditorAction.OnSaveClick)
            assertThat(awaitItem()).isEqualTo(GoalEditorEvent.GoalSaved)
        }
        val goal = repository.goals.value.single().goal
        assertThat(goal.target).isEqualTo(40_000L)
        assertThat(goal.type).isEqualTo(GoalType.DISTANCE)
        assertThat(goal.period).isEqualTo(GoalPeriod.WEEKLY)
        assertThat(repository.created).isEmpty()
    }

    // Delete

    @Test
    fun `delete opens the dialog and cancel closes it keeping the goal`() {
        repository.goals.value = listOf(trackedGoal(id = "existing"))
        val viewModel = editViewModel()

        viewModel.onAction(GoalEditorAction.OnDeleteClick)
        assertThat(viewModel.state.value.isDeleteDialogVisible).isTrue()

        viewModel.onAction(GoalEditorAction.OnDeleteDismiss)
        assertThat(viewModel.state.value.isDeleteDialogVisible).isFalse()
        assertThat(repository.goals.value.size).isEqualTo(1)
    }

    @Test
    fun `confirming delete removes the goal and emits deleted`() = runTest {
        repository.goals.value = listOf(trackedGoal(id = "existing"))
        val viewModel = editViewModel()
        viewModel.onAction(GoalEditorAction.OnDeleteClick)

        viewModel.events.test {
            viewModel.onAction(GoalEditorAction.OnDeleteConfirm)
            assertThat(awaitItem()).isEqualTo(GoalEditorEvent.GoalDeleted)
        }
        assertThat(repository.goals.value).isEmpty()
        assertThat(viewModel.state.value.isDeleteDialogVisible).isFalse()
    }

    @Test
    fun `delete is not available for a new goal`() {
        val viewModel = newGoalViewModel()
        viewModel.onAction(GoalEditorAction.OnDeleteClick)
        assertThat(viewModel.state.value.isDeleteDialogVisible).isFalse()
    }

    // Process death

    @Test
    fun `form is restored from saved state after process death`() {
        val handle = SavedStateHandle()
        val viewModel = newGoalViewModel(handle)
        viewModel.onAction(GoalEditorAction.OnTypeSelect(GoalType.FREQUENCY))
        viewModel.onAction(GoalEditorAction.OnPeriodSelect(GoalPeriod.MONTHLY))
        viewModel.type("4")

        val restored = newGoalViewModel(SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) }))

        val state = restored.state.value
        assertThat(state.type).isEqualTo(GoalType.FREQUENCY)
        assertThat(state.period).isEqualTo(GoalPeriod.MONTHLY)
        assertThat(state.targetText).isEqualTo("4")
        assertThat(state.canSave).isTrue()
    }
}
