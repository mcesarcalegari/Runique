package com.plcoding.goals.presentation.goal_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_list.components.goalTextStyle

/**
 * "Target" section of Figma frames 06/07: a 12dp-rounded surface field with the value on the
 * left and the unit for [type] on the right, plus helper text below. Input filtering happens
 * in the ViewModel; this component only reports changes.
 */
@Composable
fun GoalTargetField(
    text: String,
    onTextChange: (String) -> Unit,
    type: GoalType,
    helperText: String,
    modifier: Modifier = Modifier
) {
    val label = stringResource(R.string.goal_target)
    GoalFormSection(
        label = label,
        modifier = modifier,
        footer = { GoalHelperText(helperText) }
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            singleLine = true,
            textStyle = goalTextStyle(FontWeight.Medium, 16.sp).copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(
                keyboardType = when (type) {
                    GoalType.DISTANCE -> KeyboardType.Decimal
                    GoalType.DURATION, GoalType.FREQUENCY -> KeyboardType.Number
                },
                imeAction = ImeAction.Done
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (text.isEmpty()) {
                            Text(
                                text = stringResource(R.string.goal_target_hint),
                                style = goalTextStyle(FontWeight.Medium, 16.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(
                            when (type) {
                                GoalType.DISTANCE -> R.string.goal_unit_km
                                GoalType.DURATION -> R.string.goal_unit_hours
                                GoalType.FREQUENCY -> R.string.goal_unit_runs
                            }
                        ),
                        style = goalTextStyle(FontWeight.Normal, 14.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun GoalTargetFieldPreview(
    text: String,
    type: GoalType,
    helperText: String = "Total distance to cover during the period."
) {
    RuniqueTheme {
        GoalTargetField(
            text = text,
            onTextChange = {},
            type = type,
            helperText = helperText,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalTargetFieldDistancePreview() {
    GoalTargetFieldPreview(text = "30", type = GoalType.DISTANCE)
}

@Preview(widthDp = 360)
@Composable
private fun GoalTargetFieldDurationPreview() {
    GoalTargetFieldPreview(
        text = "10",
        type = GoalType.DURATION,
        helperText = "Total running time to reach during the period."
    )
}

@Preview(widthDp = 360)
@Composable
private fun GoalTargetFieldFrequencyPreview() {
    GoalTargetFieldPreview(
        text = "5",
        type = GoalType.FREQUENCY,
        helperText = "Number of runs to complete during the period."
    )
}

@Preview(widthDp = 360)
@Composable
private fun GoalTargetFieldEmptyPreview() {
    GoalTargetFieldPreview(text = "", type = GoalType.DISTANCE)
}

@Preview(widthDp = 360)
@Composable
private fun GoalTargetFieldEditHelperPreview() {
    GoalTargetFieldPreview(
        text = "40",
        type = GoalType.DISTANCE,
        helperText = "Only the target value can be edited."
    )
}
