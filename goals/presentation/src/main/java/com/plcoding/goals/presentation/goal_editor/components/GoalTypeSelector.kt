package com.plcoding.goals.presentation.goal_editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.components.label

/** "Goal type" section of Figma frames 06 (selectable) and 07 (locked). */
@Composable
fun GoalTypeSelector(
    selectedType: GoalType,
    onTypeSelect: (GoalType) -> Unit,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    GoalFormSection(
        label = stringResource(R.string.goal_type),
        modifier = modifier,
        footer = if (isLocked) {
            { GoalLockedHint(stringResource(R.string.goal_type_locked)) }
        } else null
    ) {
        GoalSegmentedSelector(
            options = GoalType.entries,
            selected = selectedType,
            label = { it.label },
            onSelect = onTypeSelect,
            isLocked = isLocked
        )
    }
}

@Composable
private fun GoalTypeSelectorPreview(type: GoalType, isLocked: Boolean = false) {
    RuniqueTheme {
        GoalTypeSelector(
            selectedType = type,
            onTypeSelect = {},
            isLocked = isLocked,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalTypeSelectorDistancePreview() {
    GoalTypeSelectorPreview(GoalType.DISTANCE)
}

@Preview(widthDp = 360)
@Composable
private fun GoalTypeSelectorDurationPreview() {
    GoalTypeSelectorPreview(GoalType.DURATION)
}

@Preview(widthDp = 360)
@Composable
private fun GoalTypeSelectorFrequencyPreview() {
    GoalTypeSelectorPreview(GoalType.FREQUENCY)
}

@Preview(widthDp = 360)
@Composable
private fun GoalTypeSelectorLockedPreview() {
    GoalTypeSelectorPreview(GoalType.DISTANCE, isLocked = true)
}
