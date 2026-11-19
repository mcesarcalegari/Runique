package com.plcoding.core.domain.user

/**
 * Deletes all locally stored data that belongs to the signed-in user (runs, goals, ...).
 * Pending-sync bookkeeping is not touched.
 */
interface UserDataCleaner {
    suspend fun clearAll()
}
