package com.plcoding.goals.presentation.goal_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_list.GoalsTab

/** Active / Completed / Missed filter chips from Figma frames 02–05. */
@Composable
fun GoalsTabRow(
    selectedTab: GoalsTab,
    onTabSelect: (GoalsTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .selectableGroup()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GoalsTab.entries.forEach { tab ->
            GoalsTabChip(
                text = stringResource(tab.titleRes),
                isSelected = tab == selectedTab,
                onClick = { onTabSelect(tab) }
            )
        }
    }
}

private val GoalsTab.titleRes: Int
    get() = when (this) {
        GoalsTab.ACTIVE -> R.string.goals_tab_active
        GoalsTab.COMPLETED -> R.string.goals_tab_completed
        GoalsTab.MISSED -> R.string.goals_tab_missed
    }

@Composable
private fun GoalsTabChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
                }
            )
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = goalTextStyle(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                fontSize = 12.sp
            ),
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun GoalsTabRowPreview(selectedTab: GoalsTab) {
    RuniqueTheme {
        GoalsTabRow(
            selectedTab = selectedTab,
            onTabSelect = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
        )
    }
}

@Preview
@Composable
private fun GoalsTabRowActivePreview() {
    GoalsTabRowPreview(GoalsTab.ACTIVE)
}

@Preview
@Composable
private fun GoalsTabRowCompletedPreview() {
    GoalsTabRowPreview(GoalsTab.COMPLETED)
}

@Preview
@Composable
private fun GoalsTabRowMissedPreview() {
    GoalsTabRowPreview(GoalsTab.MISSED)
}
