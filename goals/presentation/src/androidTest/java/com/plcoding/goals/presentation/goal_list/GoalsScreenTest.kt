package com.plcoding.goals.presentation.goal_list

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.FakeGoalRepository
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.trackedGoal
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val repository = FakeGoalRepository()
    private val editedGoalIds = mutableListOf<String>()
    private var addGoalClicks = 0
    private var backClicks = 0

    private fun string(id: Int, vararg args: Any) = composeRule.activity.getString(id, *args)

    private fun tab(id: Int) = composeRule.onNode(hasText(string(id)) and isSelectable())

    private fun title(period: Int, type: Int) =
        string(R.string.goal_card_title, string(period), string(type))

    @Before
    fun setUp() {
        repository.goals.value = listOf(
            trackedGoal(id = "active", type = GoalType.DISTANCE, period = GoalPeriod.WEEKLY),
            trackedGoal(
                id = "completed",
                type = GoalType.FREQUENCY,
                period = GoalPeriod.MONTHLY,
                target = 12,
                progress = 12,
                status = GoalStatus.COMPLETED,
                daysLeft = -7
            ),
            trackedGoal(
                id = "missed",
                type = GoalType.DURATION,
                period = GoalPeriod.WEEKLY,
                target = 21_600,
                progress = 16_200,
                status = GoalStatus.MISSED,
                daysLeft = -11
            )
        )
        composeRule.setContent {
            RuniqueTheme {
                GoalsScreenRoot(
                    onBackClick = { backClicks++ },
                    onAddGoalClick = { addGoalClicks++ },
                    onEditGoalClick = { editedGoalIds += it },
                    viewModel = GoalsViewModel(repository)
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun activeTab_isSelectedByDefault_andShowsOnlyActiveGoals() {
        tab(R.string.goals_tab_active).assertIsSelected()
        composeRule.onNodeWithText(title(R.string.goal_period_weekly, R.string.goal_type_distance))
            .assertIsDisplayed()
        composeRule.onNodeWithText(title(R.string.goal_period_monthly, R.string.goal_type_frequency))
            .assertDoesNotExist()
    }

    @Test
    fun switchingTabs_showsThatTabsGoals() {
        tab(R.string.goals_tab_missed).performClick()
        composeRule.waitForIdle()

        tab(R.string.goals_tab_missed).assertIsSelected()
        composeRule.onNodeWithText(title(R.string.goal_period_weekly, R.string.goal_type_duration))
            .assertIsDisplayed()
        composeRule.onNodeWithText(title(R.string.goal_period_weekly, R.string.goal_type_distance))
            .assertDoesNotExist()

        tab(R.string.goals_tab_completed).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(title(R.string.goal_period_monthly, R.string.goal_type_frequency))
            .assertIsDisplayed()
    }

    @Test
    fun emptyTab_showsEmptyState() {
        repository.goals.value = emptyList()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(string(R.string.goals_empty_active_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.goals_empty_active_description)).assertIsDisplayed()
    }

    @Test
    fun tappingActiveCard_opensEditForThatGoal() {
        composeRule.onNodeWithText(title(R.string.goal_period_weekly, R.string.goal_type_distance))
            .performClick()

        assertThat(editedGoalIds).containsExactly("active")
    }

    @Test
    fun tappingCompletedCard_doesNotNavigate() {
        tab(R.string.goals_tab_completed).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(title(R.string.goal_period_monthly, R.string.goal_type_frequency))
            .performClick()

        assertThat(editedGoalIds).isEmpty()
    }

    @Test
    fun tappingFab_fromCompletedTab_opensNewGoal() {
        tab(R.string.goals_tab_completed).performClick()
        composeRule.onNodeWithContentDescription(string(R.string.add_goal)).performClick()

        assertThat(addGoalClicks).isEqualTo(1)
    }

    @Test
    fun tappingBack_navigatesBack() {
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(com.plcoding.core.presentation.designsystem.R.string.go_back)
        ).performClick()

        assertThat(backClicks).isEqualTo(1)
    }
}
