package com.plcoding.goals.domain

/**
 * A [Goal] with its progress computed from the logged runs.
 *
 * @param progress in the same base unit as [Goal.target].
 * @param percent progress / target * 100, rounded down, not capped.
 * @param daysLeft whole days from today until the period's last day (0 on the last day,
 * negative once the period has ended).
 */
data class TrackedGoal(
    val goal: Goal,
    val progress: Long,
    val percent: Long,
    val status: GoalStatus,
    val daysLeft: Long
)
