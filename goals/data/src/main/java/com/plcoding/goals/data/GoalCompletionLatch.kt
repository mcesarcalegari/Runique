package com.plcoding.goals.data

import com.plcoding.core.database.dao.GoalDao
import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.core.domain.run.Run
import com.plcoding.goals.data.mappers.toGoal
import java.time.ZonedDateTime

/** Writes the completion time of every goal that has just reached its target (design D5). */
internal suspend fun GoalDao.latchReachedGoals(
    goals: List<GoalEntity>,
    runs: List<Run>,
    now: ZonedDateTime
) {
    goals
        .filter { GoalProgressCalculator.shouldLatch(it.toGoal(), runs, now) }
        .forEach { setCompletedAt(it.id, now.toInstant().toString()) }
}
