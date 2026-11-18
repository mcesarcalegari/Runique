package com.plcoding.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.plcoding.core.database.dao.GoalDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalDaoTest {

    private lateinit var db: RunDatabase
    private lateinit var dao: GoalDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RunDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.goalDao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsert_emitsThroughObserveAll() = runTest {
        dao.observeAll().test {
            assertThat(awaitItem()).isEmpty()
            dao.upsert(goal(id = "a"))
            assertThat(awaitItem().map { it.id }).containsExactly("a")
        }
    }

    @Test
    fun upsert_existingId_replacesRow() = runTest {
        dao.upsert(goal(id = "a"))
        dao.upsert(goal(id = "a").copy(target = 40_000))

        assertThat(dao.getById("a")?.target).isEqualTo(40_000L)
    }

    @Test
    fun getById_unknownId_returnsNull() = runTest {
        assertThat(dao.getById("missing")).isNull()
    }

    @Test
    fun setCompletedAt_setsValueOnce() = runTest {
        dao.upsert(goal(id = "a"))

        dao.setCompletedAt("a", "2024-03-08T10:00:00Z")
        dao.setCompletedAt("a", "2024-03-09T10:00:00Z")

        assertThat(dao.getById("a")?.completedAtUtc).isEqualTo("2024-03-08T10:00:00Z")
    }

    @Test
    fun deleteById_removesOnlyThatGoal() = runTest {
        dao.upsert(goal(id = "a"))
        dao.upsert(goal(id = "b"))

        dao.deleteById("a")

        dao.observeAll().test {
            assertThat(awaitItem().map { it.id }).containsExactly("b")
        }
    }

    @Test
    fun deleteAll_removesEveryGoal() = runTest {
        dao.upsert(goal(id = "a"))
        dao.upsert(goal(id = "b"))

        dao.deleteAll()

        dao.observeAll().test {
            assertThat(awaitItem()).isEmpty()
        }
    }
}
