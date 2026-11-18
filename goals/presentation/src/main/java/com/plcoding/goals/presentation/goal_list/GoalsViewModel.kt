package com.plcoding.goals.presentation.goal_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.plcoding.goals.domain.GoalRepository
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.presentation.goal_list.mapper.sortedForTab
import com.plcoding.goals.presentation.goal_list.mapper.toGoalUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class GoalsViewModel(
    goalRepository: GoalRepository
) : ViewModel() {

    private val selectedTab = MutableStateFlow(GoalsTab.ACTIVE)

    val state: StateFlow<GoalsState> = combine(
        goalRepository.observeGoals(),
        selectedTab
    ) { goals, tab ->
        GoalsState(
            selectedTab = tab,
            isLoading = false,
            activeGoals = goals.sortedForTab(GoalStatus.ACTIVE).map { it.toGoalUi() },
            completedGoals = goals.sortedForTab(GoalStatus.COMPLETED).map { it.toGoalUi() },
            missedGoals = goals.sortedForTab(GoalStatus.MISSED).map { it.toGoalUi() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalsState()
    )

    fun onAction(action: GoalsAction) {
        when (action) {
            is GoalsAction.OnTabSelect -> selectedTab.update { action.tab }
            // Navigation is handled by the Root composable.
            is GoalsAction.OnGoalClick,
            GoalsAction.OnAddGoalClick,
            GoalsAction.OnBackClick -> Unit
        }
    }
}
