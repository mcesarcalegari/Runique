package com.plcoding.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.plcoding.core.database.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Query("SELECT * FROM goalentity")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goalentity WHERE id=:id")
    suspend fun getById(id: String): GoalEntity?

    @Upsert
    suspend fun upsert(goal: GoalEntity)

    @Query("DELETE FROM goalentity WHERE id=:id")
    suspend fun deleteById(id: String)

    @Query("UPDATE goalentity SET completedAtUtc=:completedAtUtc WHERE id=:id AND completedAtUtc IS NULL")
    suspend fun setCompletedAt(id: String, completedAtUtc: String)

    @Query("DELETE FROM goalentity")
    suspend fun deleteAll()
}
