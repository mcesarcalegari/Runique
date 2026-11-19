package com.plcoding.goals.domain

import java.time.Instant
import java.time.LocalDate

typealias GoalId = String

/**
 * A one-time goal covering exactly one calendar period.
 *
 * @param target in base units of [type]: metres, seconds or runs.
 * @param periodStart first local day of the period (Monday or the 1st).
 * @param periodEnd last local day of the period (Sunday or the last day of the month).
 * @param completedAt set once, when progress first reached [target].
 */
data class Goal(
    val id: GoalId,
    val type: GoalType,
    val period: GoalPeriod,
    val target: Long,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val createdAt: Instant,
    val completedAt: Instant?
)
