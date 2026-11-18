package com.plcoding.goals.presentation.goal_editor

import androidx.compose.runtime.Immutable
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType

/**
 * Shared state of the New goal (Figma frame 06) and Edit goal (frames 07, 08) screens.
 * In edit mode type and period are locked and only the target can change.
 */
@Immutable
data class GoalEditorState(
    val isEditMode: Boolean = false,
    val isLoadingGoal: Boolean = false,
    val type: GoalType = GoalType.DISTANCE,
    val period: GoalPeriod = GoalPeriod.WEEKLY,
    val targetText: String = "",
    val canSave: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleteDialogVisible: Boolean = false,
    val isDeleting: Boolean = false
)
