package com.plcoding.goals.presentation.goal_editor

import com.plcoding.core.presentation.ui.UiText

sealed interface GoalEditorEvent {
    data object GoalSaved : GoalEditorEvent
    data object GoalDeleted : GoalEditorEvent
    data class Error(val message: UiText) : GoalEditorEvent
}
