package com.plcoding.goals.domain

import com.plcoding.core.domain.util.Error
import com.plcoding.core.domain.util.Result

enum class TargetError : Error {
    EMPTY,
    INVALID_FORMAT,
    NOT_POSITIVE,
    TOO_LARGE
}

/**
 * Parses and validates the target text the user types for a goal (design D9):
 * - Distance: km with at most one decimal place → metres.
 * - Duration: whole hours → seconds.
 * - Frequency: whole runs.
 * The value must be greater than zero.
 */
object GoalTargetValidator {

    /** Upper bound on the integer digits, which keeps every base-unit value far from overflow. */
    const val MAX_INTEGER_DIGITS = 6

    private const val METRES_PER_KM = 1_000L
    private const val SECONDS_PER_HOUR = 3_600L

    /**
     * Whether [text] may be shown in the target field while typing. Allows partial input
     * such as "" or "12." so the user can keep typing; anything else is filtered out.
     */
    fun isAllowedInput(text: String, type: GoalType): Boolean {
        val pattern = when (type) {
            GoalType.DISTANCE -> DISTANCE_INPUT
            GoalType.DURATION, GoalType.FREQUENCY -> WHOLE_INPUT
        }
        return pattern.matches(text)
    }

    /** Parses [text] into the target in base units of [type]. */
    fun parse(text: String, type: GoalType): Result<Long, TargetError> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            return Result.Error(TargetError.EMPTY)
        }
        val pattern = when (type) {
            GoalType.DISTANCE -> DISTANCE_VALUE
            GoalType.DURATION, GoalType.FREQUENCY -> WHOLE_VALUE
        }
        val match = pattern.matchEntire(trimmed)
            ?: return Result.Error(TargetError.INVALID_FORMAT)

        val integerPart = match.groupValues[1]
        if (integerPart.trimStart('0').length > MAX_INTEGER_DIGITS) {
            return Result.Error(TargetError.TOO_LARGE)
        }
        val whole = integerPart.toLong()
        val baseValue = when (type) {
            GoalType.DISTANCE -> {
                val tenths = match.groupValues.getOrNull(2)?.takeIf { it.isNotEmpty() }?.toLong() ?: 0L
                whole * METRES_PER_KM + tenths * (METRES_PER_KM / 10)
            }
            GoalType.DURATION -> whole * SECONDS_PER_HOUR
            GoalType.FREQUENCY -> whole
        }
        return if (baseValue > 0) {
            Result.Success(baseValue)
        } else {
            Result.Error(TargetError.NOT_POSITIVE)
        }
    }

    /** Formats a target in base units back into the text the user would type. */
    fun toInputText(target: Long, type: GoalType): String {
        return when (type) {
            GoalType.DISTANCE -> {
                val tenthsOfKm = target / (METRES_PER_KM / 10)
                val whole = tenthsOfKm / 10
                val decimal = tenthsOfKm % 10
                if (decimal == 0L) "$whole" else "$whole.$decimal"
            }
            GoalType.DURATION -> "${target / SECONDS_PER_HOUR}"
            GoalType.FREQUENCY -> "$target"
        }
    }

    private val DISTANCE_INPUT = Regex("""\d{0,$MAX_INTEGER_DIGITS}(\.\d?)?""")
    private val WHOLE_INPUT = Regex("""\d{0,$MAX_INTEGER_DIGITS}""")
    private val DISTANCE_VALUE = Regex("""(\d+)(?:\.(\d?))?""")
    private val WHOLE_VALUE = Regex("""(\d+)""")
}
