package com.plcoding.goals.data.di

import com.plcoding.goals.data.GoalCompletionObserver
import com.plcoding.goals.data.RoomGoalRepository
import com.plcoding.goals.data.SystemDefaultZoneClock
import com.plcoding.goals.domain.GoalRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import java.time.Clock

val goalsDataModule = module {
    single<Clock> { SystemDefaultZoneClock }
    singleOf(::RoomGoalRepository).bind<GoalRepository>()
    singleOf(::GoalCompletionObserver)
}
