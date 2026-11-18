package com.plcoding.goals.data

import com.plcoding.core.database.dao.GoalDao
import com.plcoding.core.domain.run.RunRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.ZonedDateTime
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

/**
 * Records the completion time of goals as soon as they reach their target, whether or not
 * the Goals screen is open (design D5). Start it once for the lifetime of the process.
 *
 * Failures are logged and swallowed: the observer runs on the application scope, so an
 * uncaught exception would crash the app (and repeat on every launch while a goal is pending).
 * A failed latch is retried on the next emission.
 */
class GoalCompletionObserver(
    private val goalDao: GoalDao,
    private val runRepository: RunRepository,
    private val clock: Clock
) {

    fun start(scope: CoroutineScope): Job {
        return combine(goalDao.observeAll(), runRepository.getRuns()) { goals, runs -> goals to runs }
            .onEach { (goals, runs) ->
                try {
                    goalDao.latchReachedGoals(goals, runs, ZonedDateTime.now(clock))
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "Failed to latch reached goals")
                }
            }
            .catch { e -> Timber.e(e, "Goal completion observer stopped") }
            .launchIn(scope)
    }
}
