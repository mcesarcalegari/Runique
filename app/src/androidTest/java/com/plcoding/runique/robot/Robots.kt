package com.plcoding.runique.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import com.plcoding.goals.presentation.R as GoalsR
import com.plcoding.run.presentation.R as RunR
import com.plcoding.auth.presentation.R as AuthR
import com.plcoding.core.presentation.designsystem.R as DesignR

private const val TIMEOUT_MILLIS = 10_000L

private fun string(id: Int, vararg args: Any): String =
    InstrumentationRegistry.getInstrumentation().targetContext.getString(id, *args)

private fun ComposeTestRule.waitForText(text: String) {
    waitUntil(TIMEOUT_MILLIS) { onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
}

class RunOverviewRobot(private val rule: ComposeTestRule) {

    fun isShown() = apply { rule.waitForText(string(RunR.string.runique)) }

    fun openGoals(): GoalsRobot {
        openMenu()
        rule.onNode(hasText(string(RunR.string.goals))).performClick()
        return GoalsRobot(rule)
    }

    fun logout(): AuthRobot {
        openMenu()
        rule.onNode(hasText(string(RunR.string.logout))).performClick()
        return AuthRobot(rule)
    }

    private fun openMenu() {
        rule.onNode(hasContentDescription(string(DesignR.string.open_menu))).performClick()
        rule.waitForIdle()
    }
}

class GoalsRobot(private val rule: ComposeTestRule) {

    fun isShown() = apply { rule.waitForText(string(GoalsR.string.goals)) }

    fun addGoal(): GoalEditorRobot {
        rule.onNode(hasContentDescription(string(GoalsR.string.add_goal))).performClick()
        return GoalEditorRobot(rule)
    }

    fun assertActiveGoalShown(periodRes: Int, typeRes: Int, target: String) = apply {
        val title = string(GoalsR.string.goal_card_title, string(periodRes), string(typeRes))
        rule.waitForText(title)
        rule.onNode(hasText(title)).assertIsDisplayed()
        rule.onNode(hasText(string(GoalsR.string.goal_target_distance, target))).assertIsDisplayed()
    }

    fun assertNoActiveGoals() = apply {
        rule.waitForText(string(GoalsR.string.goals_empty_active_title))
    }
}

class GoalEditorRobot(private val rule: ComposeTestRule) {

    fun isShown() = apply { rule.waitForText(string(GoalsR.string.new_goal)) }

    fun selectType(typeRes: Int) = apply {
        rule.onNode(hasText(string(typeRes)) and hasClickAction()).performClick()
    }

    fun selectPeriod(periodRes: Int) = apply {
        rule.onNode(hasText(string(periodRes)) and hasClickAction()).performClick()
    }

    fun enterTarget(text: String) = apply {
        rule.onNode(hasContentDescription(string(GoalsR.string.goal_target))).performTextInput(text)
    }

    fun save(): GoalsRobot {
        rule.onNode(hasText(string(GoalsR.string.save_goal)) and hasClickAction()).performClick()
        return GoalsRobot(rule)
    }
}

class AuthRobot(private val rule: ComposeTestRule) {

    fun login(email: String, password: String): RunOverviewRobot {
        rule.waitForText(string(AuthR.string.sign_in))
        rule.onNode(hasText(string(AuthR.string.sign_in)) and hasClickAction()).performClick()
        rule.waitForText(string(AuthR.string.hi_there))
        val fields = rule.onAllNodes(hasSetTextAction())
        fields[0].performTextInput(email)
        fields[1].performTextInput(password)
        rule.onNode(hasText(string(AuthR.string.login)) and hasClickAction()).performClick()
        return RunOverviewRobot(rule)
    }
}
