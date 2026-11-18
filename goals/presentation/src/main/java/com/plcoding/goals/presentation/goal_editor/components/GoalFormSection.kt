package com.plcoding.goals.presentation.goal_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.core.presentation.designsystem.LockIcon
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.presentation.goal_list.components.goalTextStyle

/** A labelled form block (label 13sp, 8dp gaps) as used throughout Figma frames 06–07. */
@Composable
internal fun GoalFormSection(
    label: String,
    modifier: Modifier = Modifier,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = goalTextStyle(FontWeight.Medium, 13.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        content()
        footer?.invoke()
    }
}

/** Helper text below a form control (Regular 11sp). */
@Composable
internal fun GoalHelperText(text: String) {
    Text(
        text = text,
        style = goalTextStyle(FontWeight.Normal, 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** "Locked" hint below a disabled control in edit mode (frame 07). */
@Composable
internal fun GoalLockedHint(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = LockIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(11.dp)
        )
        Text(
            text = text,
            style = goalTextStyle(FontWeight.Medium, 11.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GoalFormPreviewSurface(content: @Composable () -> Unit) {
    RuniqueTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalFormSectionPreview() {
    GoalFormPreviewSurface {
        GoalFormSection(label = "Target") {
            Text(text = "Form control", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalFormSectionWithFooterPreview() {
    GoalFormPreviewSurface {
        GoalFormSection(
            label = "Period",
            footer = { GoalHelperText(text = "Starts Monday 00:00 local, ends Sunday 23:59.") }
        ) {
            Text(text = "Form control", color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalHelperTextPreview() {
    GoalFormPreviewSurface {
        GoalHelperText(text = "Total distance to cover during the period.")
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalLockedHintPreview() {
    GoalFormPreviewSurface {
        GoalLockedHint(text = "Only the target value can be edited.")
    }
}
