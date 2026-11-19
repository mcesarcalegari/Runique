# /verify review — add-goal-tracking

Branch `feature/track-goals` vs `master` · 2026-11-18

## Verdict
TESTS_FAILED

## Tests
- PASS `:goals:domain:test`
- PASS `:goals:data:testDebugUnitTest`
- PASS `:goals:presentation:testDebugUnitTest`
- PASS `:run:presentation:testDebugUnitTest`
- PASS `:core:database:testDebugUnitTest`
- PASS `:app:testDebugUnitTest`
- PASS `:core:domain:test` (no test sources)
- FAIL `:run:data:testDebugUnitTest`
  - `compileDebugUnitTestKotlin`: `run/data/src/test/java/com/plcoding/run/data/ExampleUnitTest.kt:3:12 Unresolved reference 'junit'` (also `Test`, `assertEquals`)
- FAIL `:core:presentation:designsystem:testDebugUnitTest`
  - `compileDebugUnitTestKotlin`: `core/presentation/designsystem/src/test/java/com/plcoding/core/presentation/designsystem/ExampleUnitTest.kt:3:12 Unresolved reference 'junit'` (also `Test`, `assertEquals`)
- PASS `:core:database:connectedDebugAndroidTest`
- PASS `:goals:data:connectedDebugAndroidTest`
- PASS `:goals:presentation:connectedDebugAndroidTest`
- PASS `:app:connectedDebugAndroidTest`

## Findings

### Bug
1. **MAJOR** — `goals/data/src/main/java/com/plcoding/goals/data/GoalCompletionObserver.kt:24` — Process-lifetime completion observer has no exception handling, so any database failure crashes the app and can repeat on every launch.
   - Evidence: `combine(goalDao.observeAll(), runRepository.getRuns()) { … }.onEach { goalDao.latchReachedGoals(goals, runs, ZonedDateTime.now(clock)) }.launchIn(scope)`. `RuniqueApp.onCreate` starts it with `get<GoalCompletionObserver>().start(applicationScope)`, where `applicationScope = CoroutineScope(SupervisorJob())` and there is no `CoroutineExceptionHandler`. `RoomGoalRepository.save()` catches `SQLiteFullException` on the same write path.
   - Why it matters: an exception thrown in `onEach` reaches the uncaught-exception handler and kills the process. That covers `SQLiteFullException` from `setCompletedAt` and a parse error in the `toGoal` mapping. While a goal that can be latched is still pending, the crash repeats on every launch. The observer also stays dead for the rest of the process, so later completions are never latched (design D5).
   - Suggested fix: wrap the latch in try/catch inside `onEach`, or add `.catch {}` / `.retry` to the flow, and log the failure.

### Convention
2. **MAJOR** — `goals/presentation/src/main/java/com/plcoding/goals/presentation/goal_editor/components/GoalFormSection.kt:21` — The non-Root composables `GoalFormSection`, `GoalHelperText` and `GoalLockedHint` have no `@Preview`.
   - Evidence: the file defines `GoalFormSection` (which renders with or without a footer), `GoalHelperText` and `GoalLockedHint`. It has 0 `@Preview`.
   - Rule: `openspec/config.yaml:24` says "every sub-composable/component) MUST have one @Preview per distinct state it can render".

## Audit
| # | Finding | Verdict | Trigger | Reason |
|---|---|---|---|---|
| 1 | GoalCompletionObserver has no exception handling | KEEP | none | Verified: lines 24-28 run combine/onEach/launchIn with no catch. `RuniqueApp.kt:31` creates `CoroutineScope(SupervisorJob())` and line 63 starts the observer. `RoomGoalRepository.kt:92` catches `SQLiteFullException`. |
| 2 | GoalFormSection.kt composables have no @Preview | KEEP | none | Verified: the file has 0 @Preview and the 3 composables sit at lines 21, 43 and 53 (the line originally cited was off). The rule text appears verbatim at `openspec/config.yaml:24`. |

## Notes
- The repo has no `CLAUDE.md`, `AGENTS.md` or constitution. The ground truth used was `openspec/config.yaml` plus the `openspec/changes/add-goal-tracking/*` docs.
- **Test failures:** both failing tasks break at compile time in template `ExampleUnitTest.kt` files. Those files are **not** in the diff and have not changed since the base module setup. This branch bumps AGP from 9.1.0 to 9.5.1 and updates the Gradle wrapper. `AndroidLibraryConventionPlugin` only adds `kotlin("test")`, so these JUnit 4 templates can no longer resolve `org.junit`. I did not check whether they also fail on `master`. The global acceptance criterion in `openspec/config.yaml` requires every unit test of affected modules to pass, so this still blocks. Possible fixes: delete the two template tests, or add JUnit to those modules (for example by applying `runique.jvm.junit5`).
- The security, behavior-deviation and coverage review found no BLOCKER/MAJOR issues. It noted 2 small gaps it left out as below MAJOR: no test checks the weekly helper text string, and no test renders the "3 days left" text.
- The bug reviewer left out one low-probability race: `updateTarget` reads the goal and then upserts the whole row. If the observer latches completion between those two steps, `completedAtUtc` is overwritten with null.
