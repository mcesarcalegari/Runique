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
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.components.label

/** "Period" section of Figma frames 06 (selectable, with helper text) and 07 (locked). */
@Composable
fun GoalPeriodSelector(
    selectedPeriod: GoalPeriod,
    onPeriodSelect: (GoalPeriod) -> Unit,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    GoalFormSection(
        label = stringResource(R.string.goal_period),
        modifier = modifier,
        footer = {
            if (isLocked) {
                GoalLockedHint(stringResource(R.string.goal_period_locked))
            } else {
                GoalHelperText(
                    stringResource(
                        when (selectedPeriod) {
                            GoalPeriod.WEEKLY -> R.string.goal_helper_weekly
                            GoalPeriod.MONTHLY -> R.string.goal_helper_monthly
                        }
                    )
                )
            }
        }
    ) {
        GoalSegmentedSelector(
            options = GoalPeriod.entries,
            selected = selectedPeriod,
            label = { it.label },
            onSelect = onPeriodSelect,
            isLocked = isLocked
        )
    }
}

@Composable
private fun GoalPeriodSelectorPreview(period: GoalPeriod, isLocked: Boolean = false) {
    RuniqueTheme {
        GoalPeriodSelector(
            selectedPeriod = period,
            onPeriodSelect = {},
            isLocked = isLocked,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalPeriodSelectorWeeklyPreview() {
    GoalPeriodSelectorPreview(GoalPeriod.WEEKLY)
}

@Preview(widthDp = 360)
@Composable
private fun GoalPeriodSelectorMonthlyPreview() {
    GoalPeriodSelectorPreview(GoalPeriod.MONTHLY)
}

@Preview(widthDp = 360)
@Composable
private fun GoalPeriodSelectorLockedPreview() {
    GoalPeriodSelectorPreview(GoalPeriod.WEEKLY, isLocked = true)
}
