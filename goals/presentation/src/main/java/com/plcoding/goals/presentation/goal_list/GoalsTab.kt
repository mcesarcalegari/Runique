package com.plcoding.goals.presentation.goal_list

import com.plcoding.goals.domain.GoalStatus

enum class GoalsTab(val status: GoalStatus) {
    ACTIVE(GoalStatus.ACTIVE),
    COMPLETED(GoalStatus.COMPLETED),
    MISSED(GoalStatus.MISSED)
}
