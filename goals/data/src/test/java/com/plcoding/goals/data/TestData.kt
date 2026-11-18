package com.plcoding.goals.data

import com.plcoding.core.domain.location.Location
import com.plcoding.core.domain.run.Run
import com.plcoding.goals.domain.Goal
import com.plcoding.goals.domain.GoalPeriod
import com.plcoding.goals.domain.GoalType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal val zone: ZoneId = ZoneId.of("Europe/Berlin")

internal fun localDateTime(text: String): ZonedDateTime = LocalDateTime.parse(text).atZone(zone)

internal fun run(
    startLocal: String,
    distanceMeters: Int = 5_000,
    duration: Duration = 30.minutes,
    id: String = startLocal
) = Run(
    id = id,
    duration = duration,
    dateTimeUtc = localDateTime(startLocal).withZoneSameInstant(ZoneId.of("UTC")),
    distanceMeters = distanceMeters,
    location = Location(0.0, 0.0),
    maxSpeedKmh = 12.0,
    totalElevationMeters = 0,
    mapPictureUrl = null,
    avgHeartRate = null,
    maxHeartRate = null
)

internal fun goal(
    type: GoalType = GoalType.DISTANCE,
    period: GoalPeriod = GoalPeriod.WEEKLY,
    target: Long = 30_000,
    start: String = "2024-03-04",
    end: String = "2024-03-10",
    completedAt: Instant? = null,
    id: String = "goal"
) = Goal(
    id = id,
    type = type,
    period = period,
    target = target,
    periodStart = LocalDate.parse(start),
    periodEnd = LocalDate.parse(end),
    createdAt = Instant.parse("2024-03-07T10:00:00Z"),
    completedAt = completedAt
)
