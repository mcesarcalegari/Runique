package com.plcoding.goals.presentation.goal_list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.plcoding.core.presentation.designsystem.CalendarIcon
import com.plcoding.core.presentation.designsystem.CheckIcon
import com.plcoding.core.presentation.designsystem.DurationIcon
import com.plcoding.core.presentation.designsystem.FlagIcon
import com.plcoding.core.presentation.designsystem.Poppins
import com.plcoding.core.presentation.designsystem.RuniqueTheme
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.R
import com.plcoding.goals.presentation.components.icon
import com.plcoding.goals.presentation.components.label
import com.plcoding.goals.presentation.goal_list.model.GoalTimeLabel
import com.plcoding.goals.presentation.goal_list.model.GoalUi

/** Goal card from Figma frames 02 (Active), 04 (Completed) and 05 (Missed). */
@Composable
fun GoalCard(
    goal: GoalUi,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val isMissed = goal.status == GoalStatus.MISSED
    val accentColor = if (isMissed) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.primary
    }
    val accentContainerColor = if (isMissed) {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    }
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GoalCardHeader(
            goal = goal,
            accentColor = accentColor,
            accentContainerColor = accentContainerColor
        )
        GoalCardProgress(
            goal = goal,
            accentColor = accentColor
        )
        GoalCardFooter(goal = goal)
    }
}

@Composable
private fun GoalCardHeader(
    goal: GoalUi,
    accentColor: Color,
    accentContainerColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentContainerColor)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = goal.type.icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = stringResource(R.string.goal_card_title, goal.period.label, goal.type.label),
                style = goalTextStyle(FontWeight.SemiBold, 15.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        when (goal.status) {
            GoalStatus.ACTIVE -> Unit
            GoalStatus.COMPLETED -> GoalStatusBadge(
                icon = CheckIcon,
                text = stringResource(R.string.goal_badge_completed),
                fontSize = 10.sp,
                contentColor = accentColor,
                containerColor = accentContainerColor
            )
            GoalStatus.MISSED -> GoalStatusBadge(
                icon = FlagIcon,
                text = stringResource(R.string.goal_badge_missed),
                fontSize = 11.sp,
                contentColor = accentColor,
                containerColor = accentContainerColor
            )
        }
    }
}

@Composable
private fun GoalStatusBadge(
    icon: ImageVector,
    text: String,
    fontSize: TextUnit,
    contentColor: Color,
    containerColor: Color
) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = goalTextStyle(FontWeight.Medium, fontSize),
            color = contentColor
        )
    }
}

@Composable
private fun GoalCardProgress(
    goal: GoalUi,
    accentColor: Color
) {
    val targetRes = when (goal.type) {
        GoalType.DISTANCE -> R.string.goal_target_distance
        GoalType.DURATION -> R.string.goal_target_duration
        GoalType.FREQUENCY -> R.string.goal_target_frequency
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.alignByBaseline()) {
                Text(
                    text = goal.progress,
                    style = goalTextStyle(FontWeight.SemiBold, 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = stringResource(targetRes, goal.target),
                    style = goalTextStyle(FontWeight.Normal, 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.alignByBaseline()
                )
            }
            Text(
                text = stringResource(R.string.goal_percent, goal.percent),
                style = goalTextStyle(FontWeight.SemiBold, 14.sp),
                color = accentColor,
                modifier = Modifier.alignByBaseline()
            )
        }
        GoalProgressBar(
            fraction = goal.progressFraction,
            color = accentColor
        )
    }
}

@Composable
private fun GoalProgressBar(
    fraction: Float,
    color: Color
) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.18f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction)
                .clip(shape)
                .background(color)
        )
    }
}

@Composable
private fun GoalCardFooter(goal: GoalUi) {
    val (timeIcon, timeText) = when (val label = goal.timeLabel) {
        is GoalTimeLabel.DaysLeft -> DurationIcon to if (label.days > 0) {
            pluralStringResource(R.plurals.goal_days_left, label.days, label.days)
        } else {
            stringResource(R.string.goal_last_day)
        }
        is GoalTimeLabel.Ends -> FlagIcon to stringResource(R.string.goal_ends, label.date)
        is GoalTimeLabel.Ended -> FlagIcon to stringResource(R.string.goal_ended, label.date)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GoalCardFooterItem(icon = timeIcon, text = timeText)
        GoalCardFooterItem(icon = CalendarIcon, text = goal.dateRange)
    }
}

@Composable
private fun GoalCardFooterItem(
    icon: ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            style = goalTextStyle(FontWeight.Normal, 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal fun goalTextStyle(fontWeight: FontWeight, fontSize: TextUnit) = TextStyle(
    fontFamily = Poppins,
    fontWeight = fontWeight,
    fontSize = fontSize
)

internal object GoalCardPreviewData {
    val activeDistance = GoalUi(
        id = "1",
        type = GoalType.DISTANCE,
        period = GoalPeriod.WEEKLY,
        status = GoalStatus.ACTIVE,
        progress = "18.4",
        target = "30",
        percent = 61,
        progressFraction = 0.61f,
        dateRange = "Mar 4 – Mar 10",
        timeLabel = GoalTimeLabel.DaysLeft(3)
    )
    val activeFrequency = activeDistance.copy(
        id = "2",
        type = GoalType.FREQUENCY,
        progress = "3",
        target = "5",
        percent = 60,
        progressFraction = 0.6f
    )
    val activeDuration = activeDistance.copy(
        id = "3",
        type = GoalType.DURATION,
        period = GoalPeriod.MONTHLY,
        progress = "4h 12m",
        target = "10",
        percent = 42,
        progressFraction = 0.42f,
        dateRange = "Mar 1 – Mar 31",
        timeLabel = GoalTimeLabel.DaysLeft(24)
    )
    val completedEnded = activeDistance.copy(
        id = "4",
        status = GoalStatus.COMPLETED,
        progress = "32.1",
        percent = 107,
        progressFraction = 1f,
        dateRange = "Feb 26 – Mar 3",
        timeLabel = GoalTimeLabel.Ended("Mar 3")
    )
    val completedMonthlyFrequency = completedEnded.copy(
        id = "5",
        type = GoalType.FREQUENCY,
        period = GoalPeriod.MONTHLY,
        progress = "12",
        target = "12",
        percent = 100,
        dateRange = "Feb 1 – Feb 29",
        timeLabel = GoalTimeLabel.Ended("Feb 29")
    )
    val completedRunning = completedEnded.copy(
        id = "6",
        progress = "31.0",
        percent = 103,
        dateRange = "Mar 4 – Mar 10",
        timeLabel = GoalTimeLabel.Ends("Mar 10")
    )
    val missedDuration = activeDistance.copy(
        id = "7",
        type = GoalType.DURATION,
        status = GoalStatus.MISSED,
        progress = "4h 30m",
        target = "6",
        percent = 75,
        progressFraction = 0.75f,
        dateRange = "Feb 19 – Feb 25",
        timeLabel = GoalTimeLabel.Ended("Feb 25")
    )
    val missedMonthlyDistance = missedDuration.copy(
        id = "8",
        type = GoalType.DISTANCE,
        period = GoalPeriod.MONTHLY,
        progress = "82.0",
        target = "120",
        percent = 68,
        progressFraction = 0.68f,
        dateRange = "Jan 1 – Jan 31",
        timeLabel = GoalTimeLabel.Ended("Jan 31")
    )
}

@Composable
private fun GoalCardPreview(goal: GoalUi) {
    RuniqueTheme {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            GoalCard(goal = goal, onClick = {})
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardActiveDistancePreview() {
    GoalCardPreview(GoalCardPreviewData.activeDistance)
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardActiveDurationPreview() {
    GoalCardPreview(GoalCardPreviewData.activeDuration)
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardActiveFrequencyPreview() {
    GoalCardPreview(GoalCardPreviewData.activeFrequency)
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardActiveLastDayPreview() {
    GoalCardPreview(GoalCardPreviewData.activeDistance.copy(timeLabel = GoalTimeLabel.DaysLeft(0)))
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardCompletedEndedPreview() {
    GoalCardPreview(GoalCardPreviewData.completedEnded)
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardCompletedRunningPreview() {
    GoalCardPreview(GoalCardPreviewData.completedRunning)
}

@Preview(widthDp = 360)
@Composable
private fun GoalCardMissedPreview() {
    GoalCardPreview(GoalCardPreviewData.missedDuration)
}

@Preview(widthDp = 360, fontScale = 1.5f)
@Composable
private fun GoalCardLargeFontPreview() {
    GoalCardPreview(GoalCardPreviewData.activeDuration)
}
