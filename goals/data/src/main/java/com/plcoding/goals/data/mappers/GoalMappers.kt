package com.plcoding.goals.data.mappers

import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.goals.domain.Goal
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType
import java.time.Instant
import java.time.LocalDate

internal fun GoalEntity.toGoal(): Goal {
    return Goal(
        id = id,
        type = GoalType.valueOf(type),
        period = GoalPeriod.valueOf(period),
        target = target,
        periodStart = LocalDate.parse(periodStartDate),
        periodEnd = LocalDate.parse(periodEndDate),
        createdAt = Instant.parse(createdAtUtc),
        completedAt = completedAtUtc?.let(Instant::parse)
    )
}

internal fun Goal.toGoalEntity(): GoalEntity {
    return GoalEntity(
        id = id,
        type = type.name,
        period = period.name,
        target = target,
        periodStartDate = periodStart.toString(),
        periodEndDate = periodEnd.toString(),
        createdAtUtc = createdAt.toString(),
        completedAtUtc = completedAt?.toString()
    )
}
