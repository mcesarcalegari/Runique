package com.plcoding.goals.data

import com.plcoding.core.database.dao.GoalDao
import com.plcoding.core.domain.run.RunRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.ZonedDateTime

/**
 * Records the completion time of goals as soon as they reach their target, whether or not
 * the Goals screen is open (design D5). Start it once for the lifetime of the process.
 */
class GoalCompletionObserver(
    private val goalDao: GoalDao,
    private val runRepository: RunRepository,
    private val clock: Clock
) {

    fun start(scope: CoroutineScope): Job {
        return combine(goalDao.observeAll(), runRepository.getRuns()) { goals, runs -> goals to runs }
            .onEach { (goals, runs) ->
                goalDao.latchReachedGoals(goals, runs, ZonedDateTime.now(clock))
            }
            .launchIn(scope)
    }
}
