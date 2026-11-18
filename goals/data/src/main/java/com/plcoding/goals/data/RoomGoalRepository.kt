package com.plcoding.goals.data

import android.database.sqlite.SQLiteFullException
import com.plcoding.core.database.dao.GoalDao
import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.core.domain.run.RunRepository
import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.core.domain.util.Result
import com.plcoding.goals.data.mappers.toGoal
import com.plcoding.goals.data.mappers.toGoalEntity
import com.plcoding.goals.domain.Goal
import com.plcoding.goals.domain.GoalId
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalRepository
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.domain.TrackedGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import org.bson.types.ObjectId
import java.time.Clock
import java.time.ZonedDateTime

class RoomGoalRepository(
    private val goalDao: GoalDao,
    private val runRepository: RunRepository,
    private val clock: Clock
) : GoalRepository {

    override fun observeGoals(): Flow<List<TrackedGoal>> {
        return combine(
            goalDao.observeAll(),
            runRepository.getRuns(),
            clock.minuteTicker()
        ) { entities, runs, now ->
            entities.map { GoalProgressCalculator.track(it.toGoal(), runs, now) }
        }.distinctUntilChanged()
    }

    override suspend fun getGoal(id: GoalId): Result<Goal, DataError.Local> {
        val entity = goalDao.getById(id) ?: return Result.Error(DataError.Local.NOT_FOUND)
        return Result.Success(entity.toGoal())
    }

    override suspend fun createGoal(
        type: GoalType,
        period: GoalPeriod,
        target: Long
    ): Result<GoalId, DataError.Local> {
        val now = ZonedDateTime.now(clock)
        val range = GoalPeriodRange.containing(now.toLocalDate(), period)
        val goal = Goal(
            id = ObjectId().toHexString(),
            type = type,
            period = period,
            target = target,
            periodStart = range.start,
            periodEnd = range.end,
            createdAt = now.toInstant(),
            completedAt = null
        )
        return save(goal.toGoalEntity()).let { result ->
            when (result) {
                is Result.Error -> result
                is Result.Success -> Result.Success(goal.id)
            }
        }
    }

    override suspend fun updateTarget(id: GoalId, target: Long): EmptyResult<DataError.Local> {
        val entity = goalDao.getById(id) ?: return Result.Error(DataError.Local.NOT_FOUND)
        return save(entity.copy(target = target))
    }

    override suspend fun deleteGoal(id: GoalId): EmptyResult<DataError.Local> {
        goalDao.deleteById(id)
        return Result.Success(Unit)
    }

    /** Persists [entity] and latches it right away if its target is already reached. */
    private suspend fun save(entity: GoalEntity): EmptyResult<DataError.Local> {
        return try {
            goalDao.upsert(entity)
            goalDao.latchReachedGoals(
                goals = listOf(entity),
                runs = runRepository.getRuns().first(),
                now = ZonedDateTime.now(clock)
            )
            Result.Success(Unit)
        } catch (e: SQLiteFullException) {
            Result.Error(DataError.Local.DISK_FULL)
        }
    }
}
