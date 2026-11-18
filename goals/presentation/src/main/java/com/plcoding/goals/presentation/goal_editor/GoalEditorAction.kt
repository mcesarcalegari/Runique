package com.plcoding.goals.presentation.goal_editor

import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType

sealed interface GoalEditorAction {
    data class OnTypeSelect(val type: GoalType) : GoalEditorAction
    data class OnPeriodSelect(val period: GoalPeriod) : GoalEditorAction
    data class OnTargetChange(val text: String) : GoalEditorAction
    data object OnSaveClick : GoalEditorAction
    data object OnDeleteClick : GoalEditorAction
    data object OnDeleteConfirm : GoalEditorAction
    data object OnDeleteDismiss : GoalEditorAction
    data object OnBackClick : GoalEditorAction
}
