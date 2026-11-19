package com.plcoding.goals.data

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

/**
 * System clock that always reports the device's *current* time zone, so goal periods are
 * evaluated in the zone the user is in right now (design D3). [Clock.systemDefaultZone]
 * would freeze the zone at creation time.
 */
internal object SystemDefaultZoneClock : Clock() {
    override fun getZone(): ZoneId = ZoneId.systemDefault()
    override fun withZone(zone: ZoneId): Clock = system(zone)
    override fun instant(): Instant = Instant.now()
}
