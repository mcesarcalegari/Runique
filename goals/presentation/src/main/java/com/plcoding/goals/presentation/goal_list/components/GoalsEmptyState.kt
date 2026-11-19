package com.plcoding.goals.presentation.goal_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.core.presentation.designsystem.CheckIcon
import com.plcoding.core.presentation.designsystem.FlagIcon
import com.plcoding.core.presentation.designsystem.GoalTargetIcon
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.goal_list.GoalsTab

/**
 * Empty tab content. Active follows Figma frame 03; Completed and Missed reuse its layout
 * with placeholder icon and copy until they are designed (TODO(design)).
 */
@Composable
fun GoalsEmptyState(
    tab: GoalsTab,
    modifier: Modifier = Modifier
) {
    val (icon, titleRes, descriptionRes) = when (tab) {
        GoalsTab.ACTIVE -> Triple(
            GoalTargetIcon,
            R.string.goals_empty_active_title,
            R.string.goals_empty_active_description
        )
        GoalsTab.COMPLETED -> Triple(
            CheckIcon,
            R.string.goals_empty_completed_title,
            R.string.goals_empty_completed_description
        )
        GoalsTab.MISSED -> Triple(
            FlagIcon,
            R.string.goals_empty_missed_title,
            R.string.goals_empty_missed_description
        )
    }
    Column(
        modifier = modifier.padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        EmptyStateIcon(icon = icon)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(titleRes),
            style = goalTextStyle(FontWeight.SemiBold, 15.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(descriptionRes),
            style = goalTextStyle(FontWeight.Normal, 13.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStateIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun GoalsEmptyStatePreview(tab: GoalsTab) {
    RuniqueTheme {
        GoalsEmptyState(
            tab = tab,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        )
    }
}

@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun GoalsEmptyStateActivePreview() {
    GoalsEmptyStatePreview(GoalsTab.ACTIVE)
}

@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun GoalsEmptyStateCompletedPreview() {
    GoalsEmptyStatePreview(GoalsTab.COMPLETED)
}

@Preview(widthDp = 360, heightDp = 400)
@Composable
private fun GoalsEmptyStateMissedPreview() {
    GoalsEmptyStatePreview(GoalsTab.MISSED)
}
