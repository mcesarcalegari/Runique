package com.plcoding.goals.data

import com.plcoding.goals.domain.GoalPeriod
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/** Inclusive local-date range of a goal period. */
internal data class GoalPeriodRange(
    val start: LocalDate,
    val end: LocalDate
) {
    companion object {
        /** The calendar week (Mon–Sun) or month containing [today] (design D3). */
        fun containing(today: LocalDate, period: GoalPeriod): GoalPeriodRange {
            return when (period) {
                GoalPeriod.WEEKLY -> GoalPeriodRange(
                    start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                    end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                )
                GoalPeriod.MONTHLY -> GoalPeriodRange(
                    start = today.with(TemporalAdjusters.firstDayOfMonth()),
                    end = today.with(TemporalAdjusters.lastDayOfMonth())
                )
            }
        }
    }
}
