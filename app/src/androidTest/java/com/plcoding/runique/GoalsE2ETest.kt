package com.plcoding.runique

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.plcoding.core.domain.AuthInfo
import com.plcoding.core.domain.SessionStorage
import com.plcoding.core.domain.user.UserDataCleaner
import com.plcoding.goals.presentation.R as GoalsR
import com.plcoding.runique.robot.RunOverviewRobot
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.get

/**
 * Full app with a fake HTTP backend: Run Overview → Goals → create a goal → logout → login.
 */
@RunWith(AndroidJUnit4::class)
class GoalsE2ETest {

    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() = runBlocking {
        get<UserDataCleaner>(UserDataCleaner::class.java).clearAll()
        get<SessionStorage>(SessionStorage::class.java).set(
            AuthInfo(accessToken = "access", refreshToken = "refresh", userId = "user")
        )
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun createGoal_showsInActive_andLogoutClearsGoals() {
        RunOverviewRobot(composeRule)
            .isShown()
            .openGoals()
            .isShown()
            .assertNoActiveGoals()
            .addGoal()
            .isShown()
            .selectType(GoalsR.string.goal_type_distance)
            .enterTarget("30")
            .selectPeriod(GoalsR.string.goal_period_weekly)
            .save()
            .isShown()
            .assertActiveGoalShown(
                periodRes = GoalsR.string.goal_period_weekly,
                typeRes = GoalsR.string.goal_type_distance,
                target = "30"
            )

        pressBack()

        RunOverviewRobot(composeRule)
            .isShown()
            .logout()
            .login(email = "runner@test.com", password = "Test12345")
            .isShown()
            .openGoals()
            .isShown()
            .assertNoActiveGoals()
    }

    private fun pressBack() {
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }
}
