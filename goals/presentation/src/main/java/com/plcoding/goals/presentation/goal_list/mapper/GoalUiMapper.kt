package com.plcoding.goals.presentation.goal_list.mapper

import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalTargetValidator
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.domain.TrackedGoal
import com.plcoding.goals.presentation.goal_list.model.GoalTimeLabel
import com.plcoding.goals.presentation.goal_list.model.GoalUi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L
private const val METRES_PER_TENTH_KM = 100L

fun TrackedGoal.toGoalUi(locale: Locale = Locale.getDefault()): GoalUi {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d", locale)
    fun LocalDate.formatted() = format(dateFormatter)

    val periodEnd = goal.periodEnd.formatted()
    return GoalUi(
        id = goal.id,
        type = goal.type,
        period = goal.period,
        status = status,
        progress = formatProgress(progress, goal.type),
        target = GoalTargetValidator.toInputText(goal.target, goal.type),
        percent = percent,
        progressFraction = (percent / 100f).coerceIn(0f, 1f),
        dateRange = "${goal.periodStart.formatted()} – $periodEnd",
        timeLabel = when {
            status == GoalStatus.ACTIVE -> GoalTimeLabel.DaysLeft(daysLeft.toInt())
            daysLeft >= 0 -> GoalTimeLabel.Ends(periodEnd)
            else -> GoalTimeLabel.Ended(periodEnd)
        }
    )
}

/** Distance in km with one decimal (rounded down), duration as "Xh Ym", frequency as a count. */
internal fun formatProgress(progress: Long, type: GoalType): String {
    return when (type) {
        GoalType.DISTANCE -> {
            val tenths = progress / METRES_PER_TENTH_KM
            "${tenths / 10}.${tenths % 10}"
        }
        GoalType.DURATION -> {
            val hours = progress / SECONDS_PER_HOUR
            val minutes = (progress % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
            "${hours}h ${minutes}m"
        }
        GoalType.FREQUENCY -> "$progress"
    }
}

/** Active goals soonest-ending first; Completed and Missed most recent period first. */
internal fun List<TrackedGoal>.sortedForTab(status: GoalStatus): List<TrackedGoal> {
    val byEnd = compareBy<TrackedGoal>({ it.goal.periodEnd }, { it.goal.createdAt })
    return filter { it.status == status }
        .sortedWith(if (status == GoalStatus.ACTIVE) byEnd else byEnd.reversed())
}
