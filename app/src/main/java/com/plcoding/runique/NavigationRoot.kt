package com.plcoding.runique

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.plcoding.auth.presentation.intro.IntroScreenRoot
import com.plcoding.auth.presentation.login.LoginScreenRoot
import com.plcoding.auth.presentation.register.RegisterScreenRoot
import com.plcoding.core.notification.ActiveRunService
import com.plcoding.goals.presentation.goal_editor.GoalEditorScreenRoot
import com.plcoding.goals.presentation.goal_list.GoalsScreenRoot
import com.plcoding.run.presentation.active_run.ActiveRunScreenRoot
import com.plcoding.run.presentation.run_overview.RunOverviewScreenRoot

@Composable
fun NavigationRoot(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onAnalyticsClick: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.Run else Routes.Auth
    ) {
        authGraph(navController)
        runGraph(navController, onAnalyticsClick)
        goalsGraph(navController)
    }
}

private fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<Routes.Auth>(
        startDestination = Routes.Intro,
    ) {
        composable<Routes.Intro> {
            IntroScreenRoot(
                onSignUpClick = {
                    navController.navigate(Routes.Register)
                },
                onSignInClick = {
                    navController.navigate(Routes.Login)
                }
            )
        }
        composable<Routes.Register> {
            RegisterScreenRoot(
                onSignInClick = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Register) {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                },
                onSuccessfulRegistration = {
                    navController.navigate(Routes.Login)
                }
            )
        }
        composable<Routes.Login> {
            LoginScreenRoot(
                onLoginSuccess = {
                    navController.navigate(Routes.Run) {
                        popUpTo(Routes.Auth) {
                            inclusive = true
                        }
                    }
                },
                onSignUpClick = {
                    navController.navigate(Routes.Register) {
                        popUpTo(Routes.Login) {
                            inclusive = true
                            saveState = true
                        }
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun NavGraphBuilder.runGraph(
    navController: NavHostController,
    onAnalyticsClick: () -> Unit
) {
    navigation<Routes.Run>(
        startDestination = Routes.RunOverview,
    ) {
        composable<Routes.RunOverview> {
            RunOverviewScreenRoot(
                onStartRunClick = {
                    navController.navigate(Routes.ActiveRun)
                },
                onAnalyticsClick = onAnalyticsClick,
                onGoalsClick = {
                    navController.navigate(Routes.Goals)
                },
                onLogoutClick = {
                    navController.navigate(Routes.Auth) {
                        popUpTo(Routes.Run) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable<Routes.ActiveRun>(
            deepLinks = listOf(
                navDeepLink<Routes.ActiveRun>(
                    basePath = "runique://active_run"
                )
            )
        ) {
            val context = LocalContext.current
            ActiveRunScreenRoot(
                onBack = {
                    navController.navigateUp()
                },
                onFinish = {
                    navController.navigateUp()
                },
                onServiceToggle = { shouldServiceRun ->
                    if (shouldServiceRun) {
                        context.startService(
                            ActiveRunService.createStartIntent(
                                context = context,
                                activityClass = MainActivity::class.java
                            )
                        )
                    } else {
                        context.startService(
                            ActiveRunService.createStopIntent(context = context)
                        )
                    }
                }
            )
        }
    }
}

private fun NavGraphBuilder.goalsGraph(navController: NavHostController) {
    composable<Routes.Goals> {
        GoalsScreenRoot(
            onBackClick = {
                navController.navigateUp()
            },
            onAddGoalClick = {
                navController.navigate(Routes.NewGoal)
            },
            onEditGoalClick = { goalId ->
                navController.navigate(Routes.EditGoal(goalId))
            }
        )
    }
    composable<Routes.NewGoal> {
        GoalEditorScreenRoot(
            onBackClick = {
                navController.navigateUp()
            },
            onFinished = {
                navController.popBackStack(Routes.Goals, inclusive = false)
            }
        )
    }
    // The goalId argument reaches GoalEditorViewModel through its SavedStateHandle.
    composable<Routes.EditGoal> {
        GoalEditorScreenRoot(
            onBackClick = {
                navController.navigateUp()
            },
            onFinished = {
                navController.popBackStack(Routes.Goals, inclusive = false)
            }
        )
    }
}
