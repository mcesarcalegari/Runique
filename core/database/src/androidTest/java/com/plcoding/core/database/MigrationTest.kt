package com.plcoding.core.database

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.core.database.migration.ALL_MIGRATIONS
import com.plcoding.core.database.migration.MIGRATION_1_2
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test.db"

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation = InstrumentationRegistry.getInstrumentation(),
        databaseClass = RunDatabase::class.java
    )

    @Test
    fun migrate1To2_keepsRuns_andCreatesEmptyWritableGoalTable() = runTest {
        helper.createDatabase(dbName, 1).apply {
            execSQL(
                """
                INSERT INTO RunEntity (durationMillis, distanceMeters, dateTimeUtc, latitude, longitude,
                    avgSpeedKmh, maxSpeedKmh, totalElevationMeters, mapPictureUrl, avgHeartRate, maxHeartRate, id)
                VALUES (1800000, 5000, '2024-03-04T07:00:00Z', 1.0, 2.0, 10.0, 12.0, 20, NULL, NULL, NULL, 'run-1')
                """.trimIndent()
            )
            close()
        }

        helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2).close()

        val db = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            RunDatabase::class.java,
            dbName
        )
            .addMigrations(*ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        try {
            val runs = db.runDao.getRuns().first()
            assertThat(runs.map { it.id }).containsExactly("run-1")
            assertThat(runs.single().distanceMeters).isEqualTo(5000)

            assertThat(db.goalDao.observeAll().first()).isEmpty()
            db.goalDao.upsert(goal(id = "goal-1"))
            assertThat(db.goalDao.getById("goal-1")).isNotNull()
        } finally {
            db.close()
        }
    }
}

internal fun goal(
    id: String,
    completedAtUtc: String? = null
) = GoalEntity(
    type = "DISTANCE",
    period = "WEEKLY",
    target = 30_000,
    periodStartDate = "2024-03-04",
    periodEndDate = "2024-03-10",
    createdAtUtc = "2024-03-07T10:00:00Z",
    completedAtUtc = completedAtUtc,
    id = id
)
