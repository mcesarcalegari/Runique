package com.plcoding.run.presentation.run_overview

import assertk.assertThat
import assertk.assertions.containsExactly
import com.plcoding.core.domain.AuthInfo
import com.plcoding.core.domain.SessionStorage
import com.plcoding.core.domain.run.Run
import com.plcoding.core.domain.run.RunId
import com.plcoding.core.domain.run.RunRepository
import com.plcoding.core.domain.run.SyncRunScheduler
import com.plcoding.core.domain.user.UserDataCleaner
import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.core.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RunOverviewViewModelTest {

    /** Records the order of the logout side effects across all collaborators. */
    private val calls = mutableListOf<String>()

    private val runRepository = object : RunRepository {
        override fun getRuns(): Flow<List<Run>> = flowOf(emptyList())
        override suspend fun fetchRuns(): EmptyResult<DataError> = Result.Success(Unit)
        override suspend fun upsertRun(run: Run, mapPicture: ByteArray): EmptyResult<DataError> =
            Result.Success(Unit)
        override suspend fun deleteRun(id: RunId) = Unit
        override suspend fun syncPendingRuns() = Unit
        override suspend fun deleteAllRuns() {
            calls += "deleteAllRuns"
        }
        override suspend fun logout(): EmptyResult<DataError.Network> {
            calls += "logout"
            return Result.Success(Unit)
        }
    }

    private val syncRunScheduler = object : SyncRunScheduler {
        override suspend fun scheduleSync(type: SyncRunScheduler.SyncType) = Unit
        override suspend fun cancelAllSyncs() {
            calls += "cancelAllSyncs"
        }
    }

    private val sessionStorage = object : SessionStorage {
        override suspend fun get(): AuthInfo? = null
        override suspend fun set(info: AuthInfo?) {
            calls += "clearSession"
        }
    }

    private val userDataCleaner = object : UserDataCleaner {
        override suspend fun clearAll() {
            calls += "clearAll"
        }
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `logout clears all user data through the cleaner`() = runTest {
        val viewModel = RunOverviewViewModel(
            runRepository = runRepository,
            syncRunScheduler = syncRunScheduler,
            applicationScope = TestScope(UnconfinedTestDispatcher(testScheduler)),
            sessionStorage = sessionStorage,
            userDataCleaner = userDataCleaner
        )

        viewModel.onAction(RunOverviewAction.OnLogoutClick)

        assertThat(calls).containsExactly("cancelAllSyncs", "clearAll", "logout", "clearSession")
    }
}
