package com.plcoding.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.plcoding.core.database.dao.AnalyticsDao
import com.plcoding.core.database.dao.GoalDao
import com.plcoding.core.database.dao.RunDao
import com.plcoding.core.database.dao.RunPendingSyncDao
import com.plcoding.core.database.entity.DeletedRunSyncEntity
import com.plcoding.core.database.entity.GoalEntity
import com.plcoding.core.database.entity.RunEntity
import com.plcoding.core.database.entity.RunPendingSyncEntity

@Database(
    entities = [
        RunEntity::class,
        RunPendingSyncEntity::class,
        DeletedRunSyncEntity::class,
        GoalEntity::class
    ],
    version = 2
)
abstract class RunDatabase : RoomDatabase() {

    abstract val runDao: RunDao
    abstract val runPendingSyncDao: RunPendingSyncDao
    abstract val analyticsDao: AnalyticsDao
    abstract val goalDao: GoalDao
}