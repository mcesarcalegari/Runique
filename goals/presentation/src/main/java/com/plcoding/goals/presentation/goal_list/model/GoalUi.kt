package com.plcoding.goals.presentation.goal_list.model

import androidx.compose.runtime.Immutable
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType

/**
 * A goal card's content with every value already formatted. Units and plurals are added by
 * the composables from string resources.
 *
 * @param progress "18.4" (km), "4h 12m" (duration) or "3" (runs).
 * @param target "30" / "12.5" (km), "10" (hours) or "5" (runs).
 * @param progressFraction progress bar fill, clamped to 0..1.
 * @param dateRange e.g. "Mar 4 – Mar 10".
 */
@Immutable
data class GoalUi(
    val id: String,
    val type: GoalType,
    val period: GoalPeriod,
    val status: GoalStatus,
    val progress: String,
    val target: String,
    val percent: Long,
    val progressFraction: Float,
    val dateRange: String,
    val timeLabel: GoalTimeLabel
) {
    val isEditable: Boolean
        get() = status == GoalStatus.ACTIVE
}

sealed interface GoalTimeLabel {
    data class DaysLeft(val days: Int) : GoalTimeLabel
    data class Ends(val date: String) : GoalTimeLabel
    data class Ended(val date: String) : GoalTimeLabel
}
