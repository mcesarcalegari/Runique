package com.plcoding.goals.presentation

import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.core.domain.util.Result
import com.plcoding.goals.domain.Goal
import com.plcoding.goals.domain.GoalId
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalRepository
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.domain.TrackedGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate

class FakeGoalRepository : GoalRepository {

    val goals = MutableStateFlow<List<TrackedGoal>>(emptyList())
    var nextError: DataError.Local? = null
    val created = mutableListOf<Triple<GoalType, GoalPeriod, Long>>()

    override fun observeGoals(): Flow<List<TrackedGoal>> = goals

    override suspend fun getGoal(id: GoalId): Result<Goal, DataError.Local> {
        val goal = goals.value.firstOrNull { it.goal.id == id }?.goal
            ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(goal)
    }

    override suspend fun createGoal(
        type: GoalType,
        period: GoalPeriod,
        target: Long
    ): Result<GoalId, DataError.Local> {
        nextError?.let { return Result.Error(it) }
        created += Triple(type, period, target)
        val id = "goal-${created.size}"
        goals.update { it + trackedGoal(id = id, type = type, period = period, target = target) }
        return Result.Success(id)
    }

    override suspend fun updateTarget(id: GoalId, target: Long): EmptyResult<DataError.Local> {
        nextError?.let { return Result.Error(it) }
        if (goals.value.none { it.goal.id == id }) return Result.Error(DataError.Local.NOT_FOUND)
        goals.update { list ->
            list.map { if (it.goal.id == id) it.copy(goal = it.goal.copy(target = target)) else it }
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteGoal(id: GoalId): EmptyResult<DataError.Local> {
        nextError?.let { return Result.Error(it) }
        goals.update { list -> list.filterNot { it.goal.id == id } }
        return Result.Success(Unit)
    }
}

fun trackedGoal(
    id: String = "goal",
    type: GoalType = GoalType.DISTANCE,
    period: GoalPeriod = GoalPeriod.WEEKLY,
    target: Long = 30_000,
    progress: Long = 0,
    percent: Long = if (target > 0) progress * 100 / target else 0,
    status: GoalStatus = GoalStatus.ACTIVE,
    start: String = "2024-03-04",
    end: String = "2024-03-10",
    daysLeft: Long = 3,
    createdAt: String = "2024-03-04T10:00:00Z"
) = TrackedGoal(
    goal = Goal(
        id = id,
        type = type,
        period = period,
        target = target,
        periodStart = LocalDate.parse(start),
        periodEnd = LocalDate.parse(end),
        createdAt = Instant.parse(createdAt),
        completedAt = null
    ),
    progress = progress,
    percent = percent,
    status = status,
    daysLeft = daysLeft
)
