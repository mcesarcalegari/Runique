package com.plcoding.goals.presentation.di

import com.plcoding.goals.presentation.goal_editor.GoalEditorViewModel
import com.plcoding.goals.presentation.goal_list.GoalsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val goalsPresentationModule = module {
    viewModelOf(::GoalsViewModel)
    viewModelOf(::GoalEditorViewModel)
}
