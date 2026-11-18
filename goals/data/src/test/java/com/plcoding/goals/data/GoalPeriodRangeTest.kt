package com.plcoding.goals.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.plcoding.goals.domain.GoalPeriod
import org.junit.jupiter.api.Test
import java.time.LocalDate

class GoalPeriodRangeTest {

    private fun range(today: String, period: GoalPeriod) =
        GoalPeriodRange.containing(LocalDate.parse(today), period)

    private fun expected(start: String, end: String) =
        GoalPeriodRange(LocalDate.parse(start), LocalDate.parse(end))

    @Test
    fun `weekly goal created mid-week covers Monday to Sunday`() {
        assertThat(range("2024-03-07", GoalPeriod.WEEKLY)).isEqualTo(expected("2024-03-04", "2024-03-10"))
    }

    @Test
    fun `weekly goal created on Monday starts that day`() {
        assertThat(range("2024-03-04", GoalPeriod.WEEKLY)).isEqualTo(expected("2024-03-04", "2024-03-10"))
    }

    @Test
    fun `weekly goal created on Sunday ends that day`() {
        assertThat(range("2024-03-10", GoalPeriod.WEEKLY)).isEqualTo(expected("2024-03-04", "2024-03-10"))
    }

    @Test
    fun `weekly goal spanning a month boundary`() {
        assertThat(range("2024-03-01", GoalPeriod.WEEKLY)).isEqualTo(expected("2024-02-26", "2024-03-03"))
    }

    @Test
    fun `weekly goal spanning a year boundary`() {
        assertThat(range("2024-12-31", GoalPeriod.WEEKLY)).isEqualTo(expected("2024-12-30", "2025-01-05"))
    }

    @Test
    fun `monthly goal created mid-month covers the calendar month`() {
        assertThat(range("2024-03-07", GoalPeriod.MONTHLY)).isEqualTo(expected("2024-03-01", "2024-03-31"))
    }

    @Test
    fun `monthly goal in leap-year February ends on the 29th`() {
        assertThat(range("2024-02-10", GoalPeriod.MONTHLY)).isEqualTo(expected("2024-02-01", "2024-02-29"))
    }

    @Test
    fun `monthly goal in non-leap February ends on the 28th`() {
        assertThat(range("2023-02-10", GoalPeriod.MONTHLY)).isEqualTo(expected("2023-02-01", "2023-02-28"))
    }

    @Test
    fun `monthly goal in a 30 day month`() {
        assertThat(range("2024-04-30", GoalPeriod.MONTHLY)).isEqualTo(expected("2024-04-01", "2024-04-30"))
    }
}
