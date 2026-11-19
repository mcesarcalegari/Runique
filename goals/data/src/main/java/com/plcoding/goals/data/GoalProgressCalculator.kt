package com.plcoding.goals.data

import com.plcoding.core.domain.run.Run
import com.plcoding.goals.domain.Goal
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.domain.TrackedGoal
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Pure progress rules for goals (design D4). All time-dependent decisions use [now], whose
 * zone is the zone the local period dates are evaluated in.
 */
internal object GoalProgressCalculator {

    fun track(goal: Goal, runs: List<Run>, now: ZonedDateTime): TrackedGoal {
        val progress = progress(goal, runs, now)
        val today = now.toLocalDate()
        return TrackedGoal(
            goal = goal,
            progress = progress,
            percent = percent(progress, goal.target),
            status = status(goal, progress, now),
            daysLeft = ChronoUnit.DAYS.between(today, goal.periodEnd)
        )
    }

    /** Sum of metres or seconds, or the number of runs, whose local start date is in the period. */
    fun progress(goal: Goal, runs: List<Run>, now: ZonedDateTime): Long {
        val runsInPeriod = runs.filter { isInPeriod(it, goal, now) }
        return when (goal.type) {
            GoalType.DISTANCE -> runsInPeriod.sumOf { it.distanceMeters.toLong() }
            GoalType.DURATION -> runsInPeriod.sumOf { it.duration.inWholeSeconds }
            GoalType.FREQUENCY -> runsInPeriod.size.toLong()
        }
    }

    /** progress / target * 100, rounded down and not capped. */
    fun percent(progress: Long, target: Long): Long {
        if (target <= 0) return 0
        return progress * 100 / target
    }

    fun status(goal: Goal, progress: Long, now: ZonedDateTime): GoalStatus {
        val hasEnded = hasEnded(goal, now)
        return when {
            goal.completedAt != null -> GoalStatus.COMPLETED
            // About to be latched; a goal whose period has ended is never latched (design D5).
            progress >= goal.target && !hasEnded -> GoalStatus.COMPLETED
            hasEnded -> GoalStatus.MISSED
            else -> GoalStatus.ACTIVE
        }
    }

    /** Whether the completion time must be written now (design D5). */
    fun shouldLatch(goal: Goal, runs: List<Run>, now: ZonedDateTime): Boolean {
        return goal.completedAt == null &&
            !hasEnded(goal, now) &&
            progress(goal, runs, now) >= goal.target
    }

    fun hasEnded(goal: Goal, now: ZonedDateTime): Boolean {
        return now.toLocalDate().isAfter(goal.periodEnd)
    }

    /** A run belongs to the period containing its local start date. */
    fun isInPeriod(run: Run, goal: Goal, now: ZonedDateTime): Boolean {
        val localStartDate = run.dateTimeUtc.withZoneSameInstant(now.zone).toLocalDate()
        return !localStartDate.isBefore(goal.periodStart) && !localStartDate.isAfter(goal.periodEnd)
    }
}
