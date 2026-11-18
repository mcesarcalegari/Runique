package com.plcoding.core.database.di

import androidx.room.Room
import com.plcoding.core.database.RoomLocalRunDataSource
import com.plcoding.core.database.RoomUserDataCleaner
import com.plcoding.core.database.RunDatabase
import com.plcoding.core.database.migration.ALL_MIGRATIONS
import com.plcoding.core.domain.run.LocalRunDataSource
import com.plcoding.core.domain.user.UserDataCleaner
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidApplication(),
            RunDatabase::class.java,
            "run.db"
        )
            .addMigrations(*ALL_MIGRATIONS)
            .build()
    }
    single { get<RunDatabase>().runDao }
    single { get<RunDatabase>().runPendingSyncDao }
    single { get<RunDatabase>().goalDao }

    singleOf(::RoomLocalRunDataSource).bind<LocalRunDataSource>()
    singleOf(::RoomUserDataCleaner).bind<UserDataCleaner>()
}