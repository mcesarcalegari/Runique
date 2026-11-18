package com.plcoding.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import com.plcoding.core.database.entity.DeletedRunSyncEntity
import com.plcoding.core.database.entity.RunEntity
import com.plcoding.core.database.entity.RunPendingSyncEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomUserDataCleanerTest {

    private lateinit var db: RunDatabase
    private lateinit var cleaner: RoomUserDataCleaner

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RunDatabase::class.java
        ).allowMainThreadQueries().build()
        cleaner = RoomUserDataCleaner(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun clearAll_deletesRunsAndGoals_keepsPendingSyncTables() = runTest {
        val run = run(id = "run-1")
        db.runDao.upsertRuns(listOf(run, run(id = "run-2")))
        db.goalDao.upsert(goal(id = "goal-1"))
        db.goalDao.upsert(goal(id = "goal-2"))
        db.runPendingSyncDao.upsertRunPendingSyncEntity(
            RunPendingSyncEntity(run = run, mapPictureBytes = byteArrayOf(1), userId = "user")
        )
        db.runPendingSyncDao.upsertDeletedRunSyncEntity(
            DeletedRunSyncEntity(runId = "run-3", userId = "user")
        )

        cleaner.clearAll()

        assertThat(db.runDao.getRuns().first()).isEmpty()
        assertThat(db.goalDao.observeAll().first()).isEmpty()
        assertThat(db.runPendingSyncDao.getAllRunPendingSyncEntities("user")).hasSize(1)
        assertThat(db.runPendingSyncDao.getAllDeletedRunSyncEntities("user")).hasSize(1)
    }

    private fun run(id: String) = RunEntity(
        durationMillis = 1_800_000,
        distanceMeters = 5_000,
        dateTimeUtc = "2024-03-04T07:00:00Z",
        latitude = 1.0,
        longitude = 2.0,
        avgSpeedKmh = 10.0,
        maxSpeedKmh = 12.0,
        totalElevationMeters = 20,
        mapPictureUrl = null,
        avgHeartRate = null,
        maxHeartRate = null,
        id = id
    )
}
