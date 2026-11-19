package com.plcoding.goals.presentation.goal_list

import androidx.compose.runtime.Immutable
import com.plcoding.goals.presentation.goal_list.model.GoalUi

@Immutable
data class GoalsState(
    val selectedTab: GoalsTab = GoalsTab.ACTIVE,
    val isLoading: Boolean = true,
    val activeGoals: List<GoalUi> = emptyList(),
    val completedGoals: List<GoalUi> = emptyList(),
    val missedGoals: List<GoalUi> = emptyList()
) {
    val visibleGoals: List<GoalUi>
        get() = when (selectedTab) {
            GoalsTab.ACTIVE -> activeGoals
            GoalsTab.COMPLETED -> completedGoals
            GoalsTab.MISSED -> missedGoals
        }
}
