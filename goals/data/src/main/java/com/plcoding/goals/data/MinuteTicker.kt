package com.plcoding.goals.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.ZonedDateTime

private const val MILLIS_PER_MINUTE = 60_000L

/** Emits "now" immediately and then at the start of every minute. */
internal fun Clock.minuteTicker(): Flow<ZonedDateTime> = flow {
    while (true) {
        emit(ZonedDateTime.now(this@minuteTicker))
        delay(MILLIS_PER_MINUTE - millis() % MILLIS_PER_MINUTE)
    }
}
