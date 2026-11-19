@file:OptIn(ExperimentalMaterial3Api::class)

package com.plcoding.goals.presentation.goal_editor

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.core.presentation.designsystem.TrashIcon
import com.plcoding.core.presentation.designsystem.components.RuniqueActionButton
import com.plcoding.core.presentation.designsystem.components.RuniqueScaffold
import com.plcoding.core.presentation.designsystem.components.RuniqueToolbar
import com.plcoding.core.presentation.ui.ObserveAsEvents
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_editor.components.DeleteGoalDialog
import com.plcoding.goals.presentation.goal_editor.components.GoalPeriodSelector
import com.plcoding.goals.presentation.goal_editor.components.GoalTargetField
import com.plcoding.goals.presentation.goal_editor.components.GoalTypeSelector
import com.plcoding.goals.presentation.goal_list.components.goalTextStyle
import org.koin.androidx.compose.koinViewModel

@Composable
fun GoalEditorScreenRoot(
    onBackClick: () -> Unit,
    onFinished: () -> Unit,
    viewModel: GoalEditorViewModel = koinViewModel()
) {
    val context = LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            GoalEditorEvent.GoalSaved,
            GoalEditorEvent.GoalDeleted -> onFinished()
            is GoalEditorEvent.Error -> Toast.makeText(
                context,
                event.message.asString(context),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    GoalEditorScreen(
        state = state,
        onAction = { action ->
            if (action == GoalEditorAction.OnBackClick) onBackClick()
            viewModel.onAction(action)
        }
    )
}

/** New goal (Figma frame 06), Edit goal (frame 07) and its delete dialog (frame 08). */
@Composable
fun GoalEditorScreen(
    state: GoalEditorState,
    onAction: (GoalEditorAction) -> Unit
) {
    RuniqueScaffold(
        topAppBar = {
            RuniqueToolbar(
                showBackButton = true,
                title = stringResource(if (state.isEditMode) R.string.edit_goal else R.string.new_goal),
                onBackClick = { onAction(GoalEditorAction.OnBackClick) }
            )
        }
    ) { padding ->
        if (state.isLoadingGoal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@RuniqueScaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                GoalTypeSelector(
                    selectedType = state.type,
                    onTypeSelect = { onAction(GoalEditorAction.OnTypeSelect(it)) },
                    isLocked = state.isEditMode
                )
                GoalTargetField(
                    text = state.targetText,
                    onTextChange = { onAction(GoalEditorAction.OnTargetChange(it)) },
                    type = state.type,
                    helperText = stringResource(
                        when {
                            state.isEditMode -> R.string.goal_helper_edit
                            state.type == GoalType.DISTANCE -> R.string.goal_helper_distance
                            state.type == GoalType.DURATION -> R.string.goal_helper_duration
                            else -> R.string.goal_helper_frequency
                        }
                    )
                )
                GoalPeriodSelector(
                    selectedPeriod = state.period,
                    onPeriodSelect = { onAction(GoalEditorAction.OnPeriodSelect(it)) },
                    isLocked = state.isEditMode
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RuniqueActionButton(
                    text = stringResource(if (state.isEditMode) R.string.save_changes else R.string.save_goal),
                    isLoading = state.isSaving,
                    enabled = state.canSave && !state.isSaving,
                    onClick = { onAction(GoalEditorAction.OnSaveClick) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (state.isEditMode) {
                    DeleteGoalButton(onClick = { onAction(GoalEditorAction.OnDeleteClick) })
                }
            }
        }
    }

    if (state.isDeleteDialogVisible) {
        DeleteGoalDialog(
            onConfirm = { onAction(GoalEditorAction.OnDeleteConfirm) },
            onDismiss = { onAction(GoalEditorAction.OnDeleteDismiss) },
            isDeleting = state.isDeleting
        )
    }
}

/** Red outlined "Delete goal" button of Figma frame 07. */
@Composable
private fun DeleteGoalButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Icon(
            imageVector = TrashIcon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.delete_goal),
            style = goalTextStyle(FontWeight.SemiBold, 14.sp)
        )
    }
}

@Composable
private fun GoalEditorScreenPreview(state: GoalEditorState) {
    RuniqueTheme {
        GoalEditorScreen(state = state, onAction = {})
    }
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalEditorScreenNewWeeklyPreview() {
    GoalEditorScreenPreview(GoalEditorState(targetText = "30", canSave = true))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalEditorScreenNewMonthlyPreview() {
    GoalEditorScreenPreview(
        GoalEditorState(
            type = GoalType.FREQUENCY,
            period = GoalPeriod.MONTHLY,
            targetText = "12",
            canSave = true
        )
    )
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalEditorScreenNewSaveDisabledPreview() {
    GoalEditorScreenPreview(GoalEditorState(targetText = "", canSave = false))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalEditorScreenEditPreview() {
    GoalEditorScreenPreview(GoalEditorState(isEditMode = true, targetText = "40", canSave = true))
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalEditorScreenEditDeleteDialogPreview() {
    GoalEditorScreenPreview(
        GoalEditorState(
            isEditMode = true,
            targetText = "40",
            canSave = true,
            isDeleteDialogVisible = true
        )
    )
}

@Preview(widthDp = 360, heightDp = 800)
@Composable
private fun GoalEditorScreenLoadingPreview() {
    GoalEditorScreenPreview(GoalEditorState(isEditMode = true, isLoadingGoal = true))
}
