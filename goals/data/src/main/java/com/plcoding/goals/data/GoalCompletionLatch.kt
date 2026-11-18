package com.plcoding.goals.data

import com.plcoding.core.database.dao.GoalDao
import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.core.domain.run.Run
import com.plcoding.goals.data.mappers.toGoal
import timber.log.Timber
import java.time.ZonedDateTime

/**
 * Writes the completion time of every goal that has just reached its target (design D5).
 * Rows that can't be mapped to a [com.plcoding.goals.domain.Goal] are skipped, so one corrupt
 * row doesn't block completion of the others.
 */
internal suspend fun GoalDao.latchReachedGoals(
    goals: List<GoalEntity>,
    runs: List<Run>,
    now: ZonedDateTime
) {
    goals
        .filter { entity ->
            val goal = runCatching { entity.toGoal() }
                .onFailure { Timber.e(it, "Skipping unreadable goal %s", entity.id) }
                .getOrNull()
                ?: return@filter false
            GoalProgressCalculator.shouldLatch(goal, runs, now)
        }
        .forEach { setCompletedAt(it.id, now.toInstant().toString()) }
}
