package com.plcoding.goals.presentation.goal_list

sealed interface GoalsAction {
    data class OnTabSelect(val tab: GoalsTab) : GoalsAction
    data class OnGoalClick(val goalId: String) : GoalsAction
    data object OnAddGoalClick : GoalsAction
    data object OnBackClick : GoalsAction
}
