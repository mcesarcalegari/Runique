package com.plcoding.goals.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.plcoding.goals.domain.GoalStatus
import com.plcoding.goals.domain.GoalType
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class GoalProgressCalculatorTest {

    private val thursday = localDateTime("2024-03-07T12:00")

    // Progress calculation

    @Test
    fun `earlier runs in the period count towards a goal created later`() {
        val tracked = GoalProgressCalculator.track(
            goal = goal(),
            runs = listOf(run("2024-03-04T07:00", distanceMeters = 5_000)),
            now = thursday
        )
        assertThat(tracked.progress).isEqualTo(5_000L)
    }

    @Test
    fun `runs outside the period are ignored`() {
        val runs = listOf(
            run("2024-03-03T23:59", distanceMeters = 7_000),
            run("2024-03-11T00:00", distanceMeters = 9_000),
            run("2024-03-05T07:00", distanceMeters = 5_000)
        )
        assertThat(GoalProgressCalculator.progress(goal(), runs, thursday)).isEqualTo(5_000L)
    }

    @Test
    fun `Monday 00 00 and Sunday 23 59 are inside the week`() {
        val runs = listOf(
            run("2024-03-04T00:00", distanceMeters = 1_000),
            run("2024-03-10T23:59", distanceMeters = 2_000)
        )
        assertThat(GoalProgressCalculator.progress(goal(), runs, thursday)).isEqualTo(3_000L)
    }

    @Test
    fun `run crossing midnight counts towards the week it started in only`() {
        val crossing = run("2024-03-10T23:40", distanceMeters = 6_000, duration = 40.minutes)
        val thisWeek = goal()
        val nextWeek = goal(start = "2024-03-11", end = "2024-03-17")
        val monday = localDateTime("2024-03-11T12:00")

        assertThat(GoalProgressCalculator.progress(thisWeek, listOf(crossing), monday)).isEqualTo(6_000L)
        assertThat(GoalProgressCalculator.progress(nextWeek, listOf(crossing), monday)).isEqualTo(0L)
    }

    @Test
    fun `run is assigned by its local start date, not its UTC date`() {
        // 2024-03-04T00:30 in Berlin is 2024-03-03T23:30 UTC.
        val run = run("2024-03-04T00:30", distanceMeters = 4_000)
        assertThat(run.dateTimeUtc.zone).isEqualTo(ZoneId.of("UTC"))
        assertThat(GoalProgressCalculator.progress(goal(), listOf(run), thursday)).isEqualTo(4_000L)
    }

    @Test
    fun `duration sums run durations in seconds`() {
        val runs = listOf(
            run("2024-03-04T07:00", duration = 1.hours),
            run("2024-03-05T07:00", duration = 3.hours + 12.minutes)
        )
        val goal = goal(type = GoalType.DURATION, target = 36_000)
        assertThat(GoalProgressCalculator.progress(goal, runs, thursday)).isEqualTo(4 * 3_600L + 12 * 60)
    }

    @Test
    fun `frequency counts runs`() {
        val runs = listOf(
            run("2024-03-04T07:00"),
            run("2024-03-05T07:00"),
            run("2024-03-06T07:00")
        )
        val goal = goal(type = GoalType.FREQUENCY, target = 5)
        assertThat(GoalProgressCalculator.progress(goal, runs, thursday)).isEqualTo(3L)
    }

    @Test
    fun `no runs means zero progress`() {
        val tracked = GoalProgressCalculator.track(goal(), emptyList(), thursday)
        assertThat(tracked.progress).isEqualTo(0L)
        assertThat(tracked.percent).isEqualTo(0L)
        assertThat(tracked.status).isEqualTo(GoalStatus.ACTIVE)
    }

    @Test
    fun `monthly goal counts the whole month`() {
        val goal = goal(start = "2024-02-01", end = "2024-02-29")
        val runs = listOf(
            run("2024-02-01T07:00", distanceMeters = 1_000),
            run("2024-02-29T22:00", distanceMeters = 2_000),
            run("2024-03-01T07:00", distanceMeters = 4_000)
        )
        assertThat(GoalProgressCalculator.progress(goal, runs, localDateTime("2024-02-10T12:00")))
            .isEqualTo(3_000L)
    }

    // Progress percentage

    @Test
    fun `percentage rounds down`() {
        assertThat(GoalProgressCalculator.percent(18_400, 30_000)).isEqualTo(61L)
    }

    @Test
    fun `percentage is not capped above target`() {
        assertThat(GoalProgressCalculator.percent(32_100, 30_000)).isEqualTo(107L)
    }

    @Test
    fun `just below target shows 99 percent and stays active`() {
        val tracked = GoalProgressCalculator.track(
            goal = goal(),
            runs = listOf(run("2024-03-05T07:00", distanceMeters = 29_900)),
            now = thursday
        )
        assertThat(tracked.percent).isEqualTo(99L)
        assertThat(tracked.status).isEqualTo(GoalStatus.ACTIVE)
    }

    // Goal lifecycle

    @Test
    fun `reaching the target mid-period completes the goal`() {
        val runs = listOf(
            run("2024-03-04T07:00", distanceMeters = 28_000),
            run("2024-03-07T07:00", distanceMeters = 3_000)
        )
        val tracked = GoalProgressCalculator.track(goal(), runs, thursday)
        assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
        assertThat(tracked.percent).isEqualTo(103L)
    }

    @Test
    fun `exactly reaching the target completes the goal`() {
        val tracked = GoalProgressCalculator.track(
            goal(),
            listOf(run("2024-03-04T07:00", distanceMeters = 30_000)),
            thursday
        )
        assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
        assertThat(tracked.percent).isEqualTo(100L)
    }

    @Test
    fun `completed goal keeps accumulating`() {
        val latched = goal(completedAt = Instant.parse("2024-03-06T10:00:00Z"))
        val runs = listOf(
            run("2024-03-04T07:00", distanceMeters = 31_000),
            run("2024-03-07T07:00", distanceMeters = 1_100)
        )
        val tracked = GoalProgressCalculator.track(latched, runs, thursday)
        assertThat(tracked.progress).isEqualTo(32_100L)
        assertThat(tracked.percent).isEqualTo(107L)
        assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
    }

    @Test
    fun `period ending below target makes the goal missed`() {
        val runs = listOf(run("2024-03-05T07:00", distanceMeters = 22_500))
        val lastDay = GoalProgressCalculator.track(goal(), runs, localDateTime("2024-03-10T23:59"))
        val nextDay = GoalProgressCalculator.track(goal(), runs, localDateTime("2024-03-11T00:00"))

        assertThat(lastDay.status).isEqualTo(GoalStatus.ACTIVE)
        assertThat(nextDay.status).isEqualTo(GoalStatus.MISSED)
        assertThat(nextDay.percent).isEqualTo(75L)
    }

    @Test
    fun `runs synced into an ended period change progress but not the missed status`() {
        val runs = listOf(run("2024-03-05T07:00", distanceMeters = 31_000))
        val tracked = GoalProgressCalculator.track(goal(), runs, localDateTime("2024-03-12T09:00"))

        assertThat(tracked.percent).isEqualTo(103L)
        assertThat(tracked.status).isEqualTo(GoalStatus.MISSED)
    }

    // Completion is permanent

    @Test
    fun `latched goal stays completed after a contributing run is deleted`() {
        val latched = goal(completedAt = Instant.parse("2024-03-06T10:00:00Z"))
        val remaining = listOf(
            run("2024-03-04T07:00", distanceMeters = 20_000),
            run("2024-03-05T07:00", distanceMeters = 6_000)
        )
        val tracked = GoalProgressCalculator.track(latched, remaining, thursday)
        assertThat(tracked.progress).isEqualTo(26_000L)
        assertThat(tracked.percent).isEqualTo(86L)
        assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
    }

    @Test
    fun `latched goal stays completed after its period ends`() {
        val latched = goal(completedAt = Instant.parse("2024-03-06T10:00:00Z"))
        val tracked = GoalProgressCalculator.track(latched, emptyList(), localDateTime("2024-04-01T09:00"))
        assertThat(tracked.status).isEqualTo(GoalStatus.COMPLETED)
    }

    // Days left

    @Test
    fun `days left counts whole days until the period end`() {
        assertThat(GoalProgressCalculator.track(goal(), emptyList(), thursday).daysLeft).isEqualTo(3L)
        assertThat(
            GoalProgressCalculator.track(goal(), emptyList(), localDateTime("2024-03-10T08:00")).daysLeft
        ).isEqualTo(0L)
    }

    // Latch

    @Test
    fun `should latch only unlatched goals at target within their period`() {
        val atTarget = listOf(run("2024-03-05T07:00", distanceMeters = 30_000))
        val below = listOf(run("2024-03-05T07:00", distanceMeters = 10_000))

        assertThat(GoalProgressCalculator.shouldLatch(goal(), atTarget, thursday)).isTrue()
        assertThat(GoalProgressCalculator.shouldLatch(goal(), below, thursday)).isFalse()
        assertThat(
            GoalProgressCalculator.shouldLatch(
                goal(completedAt = Instant.parse("2024-03-06T10:00:00Z")),
                atTarget,
                thursday
            )
        ).isFalse()
        assertThat(
            GoalProgressCalculator.shouldLatch(goal(), atTarget, localDateTime("2024-03-11T00:00"))
        ).isFalse()
    }
}
