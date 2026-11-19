package com.plcoding.goals.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.plcoding.core.presentation.designsystem.DistanceIcon
import com.plcoding.core.presentation.designsystem.DurationIcon
import com.plcoding.core.presentation.designsystem.FrequencyIcon
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType
import com.plcoding.goals.presentation.R

internal val GoalType.icon: ImageVector
    @Composable
    get() = when (this) {
        GoalType.DISTANCE -> DistanceIcon
        GoalType.DURATION -> DurationIcon
        GoalType.FREQUENCY -> FrequencyIcon
    }

internal val GoalType.label: String
    @Composable
    get() = stringResource(
        when (this) {
            GoalType.DISTANCE -> R.string.goal_type_distance
            GoalType.DURATION -> R.string.goal_type_duration
            GoalType.FREQUENCY -> R.string.goal_type_frequency
        }
    )

internal val GoalPeriod.label: String
    @Composable
    get() = stringResource(
        when (this) {
            GoalPeriod.WEEKLY -> R.string.goal_period_weekly
            GoalPeriod.MONTHLY -> R.string.goal_period_monthly
        }
    )
