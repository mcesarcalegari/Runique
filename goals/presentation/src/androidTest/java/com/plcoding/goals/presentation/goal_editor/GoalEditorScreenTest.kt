package com.plcoding.goals.presentation.goal_editor

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.FakeGoalRepository
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_list.GoalsScreenRoot
import com.plcoding.goals.presentation.goal_list.GoalsViewModel
import com.plcoding.goals.presentation.trackedGoal
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalEditorScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val repository = FakeGoalRepository()

    private fun string(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun button(id: Int) = composeRule.onNode(hasText(string(id)) and hasClickAction())

    private fun targetField() = composeRule.onNodeWithContentDescription(string(R.string.goal_target))

    /** Editor that returns to the Goals list (sharing the same repository) when finished. */
    private fun setUpFlow(goalId: String? = null) {
        composeRule.setContent {
            RuniqueTheme {
                var isEditorOpen by remember { mutableStateOf(true) }
                if (isEditorOpen) {
                    GoalEditorScreenRoot(
                        onBackClick = { isEditorOpen = false },
                        onFinished = { isEditorOpen = false },
                        viewModel = remember {
                            GoalEditorViewModel(
                                repository,
                                SavedStateHandle(listOfNotNull(goalId?.let { "goalId" to it }).toMap())
                            )
                        }
                    )
                } else {
                    GoalsScreenRoot(
                        onBackClick = {},
                        onAddGoalClick = {},
                        onEditGoalClick = {},
                        viewModel = remember { GoalsViewModel(repository) }
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun createGoal_returnsToGoals_andShowsItInActive() {
        setUpFlow()

        targetField().performTextInput("30")
        button(R.string.save_goal).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(
            hasText(string(R.string.goal_card_title, string(R.string.goal_period_weekly), string(R.string.goal_type_distance)))
        ).assertIsDisplayed()
        val created = repository.goals.value.single().goal
        assertThat(created.target).isEqualTo(30_000L)
        assertThat(created.type).isEqualTo(GoalType.DISTANCE)
        assertThat(created.period).isEqualTo(GoalPeriod.WEEKLY)
    }

    @Test
    fun switchingType_changesTheUnit() {
        setUpFlow()

        composeRule.onNode(hasText(string(R.string.goal_unit_km))).assertIsDisplayed()
        composeRule.onNode(hasText(string(R.string.goal_type_frequency)) and hasClickAction()).performClick()

        composeRule.onNode(hasText(string(R.string.goal_unit_runs))).assertIsDisplayed()
    }

    @Test
    fun save_isDisabledForEmptyOrZero() {
        setUpFlow()

        button(R.string.save_goal).assertIsNotEnabled()
        targetField().performTextInput("0")
        button(R.string.save_goal).assertIsNotEnabled()
        targetField().performTextClearance()
        targetField().performTextInput("5")
        button(R.string.save_goal).assertIsEnabled()
    }

    @Test
    fun editTarget_updatesTheGoal() {
        repository.goals.value = listOf(trackedGoal(id = "g", target = 30_000))
        setUpFlow(goalId = "g")

        targetField().performTextClearance()
        targetField().performTextInput("40")
        button(R.string.save_changes).performClick()
        composeRule.waitForIdle()

        assertThat(repository.goals.value.single().goal.target).isEqualTo(40_000L)
        composeRule.onNode(hasText(string(R.string.goals))).assertIsDisplayed()
    }

    @Test
    fun deleteThenCancel_keepsTheGoal() {
        repository.goals.value = listOf(trackedGoal(id = "g"))
        setUpFlow(goalId = "g")

        button(R.string.delete_goal).performClick()
        composeRule.onNode(hasText(string(R.string.delete_goal_title))).assertIsDisplayed()
        button(R.string.cancel).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(hasText(string(R.string.delete_goal_title))).assertDoesNotExist()
        assertThat(repository.goals.value.size).isEqualTo(1)
    }

    @Test
    fun deleteThenConfirm_removesTheGoal_andReturnsToGoals() {
        repository.goals.value = listOf(trackedGoal(id = "g"))
        setUpFlow(goalId = "g")

        button(R.string.delete_goal).performClick()
        button(R.string.delete).performClick()
        composeRule.waitForIdle()

        assertThat(repository.goals.value).isEmpty()
        composeRule.onNode(hasText(string(R.string.goals_empty_active_title))).assertIsDisplayed()
    }
}
