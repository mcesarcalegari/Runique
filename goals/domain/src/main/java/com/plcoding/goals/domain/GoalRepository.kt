package com.plcoding.goals.domain

import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    /** All goals with progress, status and days left kept up to date with runs and time. */
    fun observeGoals(): Flow<List<TrackedGoal>>

    suspend fun getGoal(id: GoalId): Result<Goal, DataError.Local>

    /** Creates a goal for the current calendar period. [target] is in base units. */
    suspend fun createGoal(
        type: GoalType,
        period: GoalPeriod,
        target: Long
    ): Result<GoalId, DataError.Local>

    suspend fun updateTarget(id: GoalId, target: Long): EmptyResult<DataError.Local>

    suspend fun deleteGoal(id: GoalId): EmptyResult<DataError.Local>
}
