package com.plcoding.goals.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.plcoding.core.domain.util.Result
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class GoalTargetValidatorTest {

    @ParameterizedTest
    @EnumSource(GoalType::class)
    fun `empty text is rejected`(type: GoalType) {
        assertThat(GoalTargetValidator.parse("", type)).isEqualTo(Result.Error(TargetError.EMPTY))
        assertThat(GoalTargetValidator.parse("   ", type)).isEqualTo(Result.Error(TargetError.EMPTY))
    }

    @ParameterizedTest
    @EnumSource(GoalType::class)
    fun `zero is not positive`(type: GoalType) {
        assertThat(GoalTargetValidator.parse("0", type)).isEqualTo(Result.Error(TargetError.NOT_POSITIVE))
        assertThat(GoalTargetValidator.parse("000", type)).isEqualTo(Result.Error(TargetError.NOT_POSITIVE))
    }

    @Test
    fun `zero point zero km is not positive`() {
        assertThat(GoalTargetValidator.parse("0.0", GoalType.DISTANCE))
            .isEqualTo(Result.Error(TargetError.NOT_POSITIVE))
    }

    @Test
    fun `whole km is converted to metres`() {
        assertThat(GoalTargetValidator.parse("30", GoalType.DISTANCE)).isEqualTo(Result.Success(30_000L))
    }

    @Test
    fun `one decimal km is converted to metres`() {
        assertThat(GoalTargetValidator.parse("12.5", GoalType.DISTANCE)).isEqualTo(Result.Success(12_500L))
        assertThat(GoalTargetValidator.parse("0.1", GoalType.DISTANCE)).isEqualTo(Result.Success(100L))
    }

    @Test
    fun `trailing decimal point is accepted as whole km`() {
        assertThat(GoalTargetValidator.parse("12.", GoalType.DISTANCE)).isEqualTo(Result.Success(12_000L))
    }

    @Test
    fun `two decimal km is invalid`() {
        assertThat(GoalTargetValidator.parse("12.55", GoalType.DISTANCE))
            .isEqualTo(Result.Error(TargetError.INVALID_FORMAT))
    }

    @Test
    fun `decimal runs are invalid`() {
        assertThat(GoalTargetValidator.parse("2.5", GoalType.FREQUENCY))
            .isEqualTo(Result.Error(TargetError.INVALID_FORMAT))
    }

    @Test
    fun `decimal hours are invalid`() {
        assertThat(GoalTargetValidator.parse("1.5", GoalType.DURATION))
            .isEqualTo(Result.Error(TargetError.INVALID_FORMAT))
    }

    @Test
    fun `whole hours are converted to seconds`() {
        assertThat(GoalTargetValidator.parse("10", GoalType.DURATION)).isEqualTo(Result.Success(36_000L))
    }

    @Test
    fun `whole runs are kept as is`() {
        assertThat(GoalTargetValidator.parse("5", GoalType.FREQUENCY)).isEqualTo(Result.Success(5L))
    }

    @ParameterizedTest
    @EnumSource(GoalType::class)
    fun `non numeric text is invalid`(type: GoalType) {
        listOf("abc", "-5", "1,5", "1e3", ".").forEach { text ->
            assertThat(GoalTargetValidator.parse(text, type)).isEqualTo(Result.Error(TargetError.INVALID_FORMAT))
        }
    }

    @ParameterizedTest
    @EnumSource(GoalType::class)
    fun `largest allowed value parses without overflow`(type: GoalType) {
        val result = GoalTargetValidator.parse("999999", type)
        val expected = when (type) {
            GoalType.DISTANCE -> 999_999_000L
            GoalType.DURATION -> 999_999L * 3_600
            GoalType.FREQUENCY -> 999_999L
        }
        assertThat(result).isEqualTo(Result.Success(expected))
    }

    @ParameterizedTest
    @EnumSource(GoalType::class)
    fun `very large values are rejected`(type: GoalType) {
        assertThat(GoalTargetValidator.parse("1000000", type)).isEqualTo(Result.Error(TargetError.TOO_LARGE))
        assertThat(GoalTargetValidator.parse("99999999999999999999999", type))
            .isEqualTo(Result.Error(TargetError.TOO_LARGE))
    }

    @Test
    fun `leading zeros do not count towards the digit limit`() {
        assertThat(GoalTargetValidator.parse("0000005", GoalType.FREQUENCY)).isEqualTo(Result.Success(5L))
    }

    @Test
    fun `distance input allows partial decimals only up to one place`() {
        assertThat(GoalTargetValidator.isAllowedInput("", GoalType.DISTANCE)).isTrue()
        assertThat(GoalTargetValidator.isAllowedInput("12", GoalType.DISTANCE)).isTrue()
        assertThat(GoalTargetValidator.isAllowedInput("12.", GoalType.DISTANCE)).isTrue()
        assertThat(GoalTargetValidator.isAllowedInput("12.5", GoalType.DISTANCE)).isTrue()
        assertThat(GoalTargetValidator.isAllowedInput("12.55", GoalType.DISTANCE)).isFalse()
        assertThat(GoalTargetValidator.isAllowedInput("1.2.3", GoalType.DISTANCE)).isFalse()
        assertThat(GoalTargetValidator.isAllowedInput("-1", GoalType.DISTANCE)).isFalse()
        assertThat(GoalTargetValidator.isAllowedInput("1234567", GoalType.DISTANCE)).isFalse()
    }

    @ParameterizedTest
    @EnumSource(value = GoalType::class, names = ["DURATION", "FREQUENCY"])
    fun `whole number input rejects decimals`(type: GoalType) {
        assertThat(GoalTargetValidator.isAllowedInput("", type)).isTrue()
        assertThat(GoalTargetValidator.isAllowedInput("5", type)).isTrue()
        assertThat(GoalTargetValidator.isAllowedInput("2.", type)).isFalse()
        assertThat(GoalTargetValidator.isAllowedInput("2.5", type)).isFalse()
        assertThat(GoalTargetValidator.isAllowedInput("1234567", type)).isFalse()
    }

    @Test
    fun `targets format back to input text`() {
        assertThat(GoalTargetValidator.toInputText(30_000, GoalType.DISTANCE)).isEqualTo("30")
        assertThat(GoalTargetValidator.toInputText(12_500, GoalType.DISTANCE)).isEqualTo("12.5")
        assertThat(GoalTargetValidator.toInputText(36_000, GoalType.DURATION)).isEqualTo("10")
        assertThat(GoalTargetValidator.toInputText(5, GoalType.FREQUENCY)).isEqualTo("5")
    }
}
