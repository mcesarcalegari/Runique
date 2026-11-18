package com.plcoding.core.database

import androidx.room.withTransaction
import com.plcoding.core.domain.user.UserDataCleaner

class RoomUserDataCleaner(
    private val database: RunDatabase
) : UserDataCleaner {

    override suspend fun clearAll() {
        database.withTransaction {
            database.runDao.deleteAllRuns()
            database.goalDao.deleteAll()
        }
    }
}
