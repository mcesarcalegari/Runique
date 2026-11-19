package com.plcoding.goals.data

import com.plcoding.core.domain.location.Location
import com.plcoding.core.domain.run.Run
import com.plcoding.core.domain.run.RunId
import com.plcoding.core.domain.run.RunRepository
import com.plcoding.core.domain.util.DataError
import com.plcoding.core.domain.util.EmptyResult
import com.plcoding.core.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.time.Duration.Companion.minutes

internal val testZone: ZoneId = ZoneId.of("Europe/Berlin")

internal fun local(text: String): ZonedDateTime = LocalDateTime.parse(text).atZone(testZone)

internal class MutableClock(var now: ZonedDateTime) : Clock() {
    override fun getZone(): ZoneId = now.zone
    override fun withZone(zone: ZoneId): Clock = MutableClock(now.withZoneSameInstant(zone))
    override fun instant(): Instant = now.toInstant()
}

internal class FakeRunRepository : RunRepository {
    val runs = MutableStateFlow<List<Run>>(emptyList())

    fun add(run: Run) = runs.update { it + run }

    override fun getRuns(): Flow<List<Run>> = runs
    override suspend fun fetchRuns(): EmptyResult<DataError> = Result.Success(Unit)
    override suspend fun upsertRun(run: Run, mapPicture: ByteArray): EmptyResult<DataError> {
        add(run)
        return Result.Success(Unit)
    }
    override suspend fun deleteRun(id: RunId) = runs.update { list -> list.filterNot { it.id == id } }
    override suspend fun syncPendingRuns() = Unit
    override suspend fun deleteAllRuns() = runs.update { emptyList() }
    override suspend fun logout(): EmptyResult<DataError.Network> = Result.Success(Unit)
}

internal fun run(id: String, startLocal: String, distanceMeters: Int) = Run(
    id = id,
    duration = 30.minutes,
    dateTimeUtc = local(startLocal).withZoneSameInstant(ZoneId.of("UTC")),
    distanceMeters = distanceMeters,
    location = Location(0.0, 0.0),
    maxSpeedKmh = 12.0,
    totalElevationMeters = 0,
    mapPictureUrl = null,
    avgHeartRate = null,
    maxHeartRate = null
)
