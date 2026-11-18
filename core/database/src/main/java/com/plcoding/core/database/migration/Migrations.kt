package com.plcoding.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `GoalEntity` (
                `type` TEXT NOT NULL,
                `period` TEXT NOT NULL,
                `target` INTEGER NOT NULL,
                `periodStartDate` TEXT NOT NULL,
                `periodEndDate` TEXT NOT NULL,
                `createdAtUtc` TEXT NOT NULL,
                `completedAtUtc` TEXT,
                `id` TEXT NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent()
        )
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
