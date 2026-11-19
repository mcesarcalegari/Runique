package com.plcoding.goals.presentation.goal_editor.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.core.presentation.designsystem.TrashIcon
import com.plcoding.core.presentation.designsystem.components.RuniqueDialog
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_list.components.goalTextStyle

/**
 * Delete confirmation of Figma frame 08, built on the design-system [RuniqueDialog].
 * The Cancel text button and red Delete pill follow the frame; the dialog body keeps
 * RuniqueDialog's layout.
 */
@Composable
fun DeleteGoalDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDeleting: Boolean,
    modifier: Modifier = Modifier
) {
    RuniqueDialog(
        title = stringResource(R.string.delete_goal_title),
        description = stringResource(R.string.delete_goal_description),
        onDismiss = onDismiss,
        modifier = modifier,
        secondaryButton = {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = goalTextStyle(FontWeight.SemiBold, 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        primaryButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = TrashIcon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.delete),
                    style = goalTextStyle(FontWeight.SemiBold, 13.sp)
                )
            }
        }
    )
}

@Preview
@Composable
private fun DeleteGoalDialogPreview() {
    RuniqueTheme {
        DeleteGoalDialog(onConfirm = {}, onDismiss = {}, isDeleting = false)
    }
}
