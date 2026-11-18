# Tasks

Figma frames (fileKey `bcIkmnAUfjg4WcR5qQUR2Y`; base URL `https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=`):
01 Run Overview menu `4012-1520` · 02 Goals Active populated `4011-1520` · 03 Goals Active empty `4013-1520` · 04 Goals Completed `4014-1520` · 05 Goals Missed `4015-1520` · 06 New Goal `4016-1520` · 07 Edit Goal `4017-1520` · 08 Delete confirm dialog `4018-1520`

## 1. Module scaffolding

- [ ] 1.1 Create `:goals:domain`, `:goals:data` and `:goals:presentation` with the existing convention plugins, register them in `settings.gradle.kts`, and wire dependencies as in design D1 (domain → `:core:domain`; data → domain, `:core:database`, `:core:domain`; presentation → domain, `:core:domain`, `:core:presentation:*`; `:app` → all three). Skill: `android-module-structure` (and `android-version-catalog` if any dependency is missing). Verify: `./gradlew :goals:domain:assemble :goals:data:assembleDebug :goals:presentation:assembleDebug` succeeds.

## 2. Database

- [ ] 2.1 Before touching `RunDatabase`, build `:core:database` on the unchanged schema and commit the generated `core/database/schemas/.../1.json` (design D10). Skill: `android-data-layer`. Verify: `1.json` exists and lists `RunEntity`, `RunPendingSyncEntity`, `DeletedRunSyncEntity`.
- [ ] 2.2 Add `GoalEntity` (fields per design D2), `GoalDao` (`observeAll`, `getById`, `upsert`, `deleteById`, `setCompletedAt`, `deleteAll`), register both in `RunDatabase`, set `version = 2`, add a manual `Migration(1, 2)`, register it in `DatabaseModule`, and commit `2.json`. Skills: `android-data-layer`, `android-di-koin`. Verify: build passes and `2.json` contains `GoalEntity`.
- [ ] 2.3 Add an instrumented migration test using `MigrationTestHelper` (v1 with a run → v2: the run is kept, `GoalEntity` is empty and writable) and an in-memory `GoalDao` test (upsert, observe, setCompletedAt, delete). Skill: `android-integration-testing`. Verify: `./gradlew :core:database:connectedDebugAndroidTest` passes.
- [ ] 2.4 Add `UserDataCleaner` to `:core:domain` and implement it in `:core:database` with `withTransaction` to delete all runs and goals (design D7), provided in Koin. Skills: `android-data-layer`, `android-di-koin`, `kotlin-coroutines`. Verify: an instrumented in-memory test shows both tables are empty after `clearAll()` and the pending-sync tables are untouched.

## 3. Goals domain

- [ ] 3.1 Add `Goal`, `GoalType`, `GoalPeriod`, `GoalStatus`, `TrackedGoal` (goal + progress, percent, status, daysLeft) and the `GoalRepository` interface (`observeGoals(): Flow<List<TrackedGoal>>`, `getGoal`, `createGoal`, `updateTarget`, `deleteGoal`) with `Result`/`EmptyResult` return types. No use-case classes (design D1). Skill: `android-error-handling`. Verify: `:goals:domain` compiles.
- [ ] 3.2 Add target parsing/validation (Distance ≤1 decimal → metres; Duration whole hours → seconds; Frequency whole runs; must be > 0; design D9) with unit tests for empty, 0, `12.5` km, `12.55` km, `2.5` runs and very large values. Skill: `android-unit-testing`. Verify: `./gradlew :goals:domain:test` passes.

## 4. Goals data

- [ ] 4.1 Implement period computation (Weekly Mon–Sun and Monthly calendar ranges from a local date, design D3) and the `internal` `GoalProgressCalculator` in `:goals:data` (progress, floored percent, status, daysLeft, run-in-period check by local start date; design D4). Unit-test every scenario in spec requirements "Goal period window", "Progress calculation", "Progress percentage", "Goal lifecycle" and "Completion is permanent": mid-week/mid-month creation, leap-year February, Sunday/Monday boundaries, run crossing midnight, 61%/107%/99% cases, Active→Completed, Active→Missed, latched goal after a run is deleted. Skill: `android-unit-testing`. Verify: `./gradlew :goals:data:testDebugUnitTest` passes.
- [ ] 4.2 Implement `RoomGoalRepository`. `observeGoals()` combines `goalDao.observeAll()`, `runRepository.getRuns()` and a minute ticker from the injected `Clock`, and emits `TrackedGoal`s built with the calculator (design D4). Also add entity↔domain mappers; `createGoal` computes the period from the `Clock`; `createGoal` and `updateTarget` check the completion latch right away against current runs (design D5). Provide `Clock` and the repository in a `goalsDataModule`. Skills: `android-data-layer`, `android-di-koin`, `kotlin-flows`. Verify: an integration test with in-memory Room, a fake `RunRepository` and a controllable `Clock` shows that: `observeGoals()` re-emits with updated progress when runs change; status flips to Missed when the clock passes the period end; and "target reached on creation" and "target lowered below progress" both persist `completedAtUtc`.
- [ ] 4.3 Implement `GoalCompletionObserver` (combines goals and runs, latches eligible goals; design D5) and start it once from `RuniqueApp.applicationScope`. Skills: `kotlin-flows`, `kotlin-coroutines`, `android-di-koin`. Verify: an integration test where a run is added (goal latched) then deleted leaves the goal Completed, and a goal whose period has ended is never latched.
- [ ] 4.4 Run the data-layer tests. Skill: `android-integration-testing`. Verify: `./gradlew :goals:data:testDebugUnitTest :goals:data:connectedDebugAndroidTest` passes.

## 5. Design-system assets

- [ ] 5.1 Export from Figma and convert the icons used by frames 01–08 that aren't already in the design system (goals target, distance, duration/clock, frequency, calendar, flag, lock, trash, check) to `ImageVector`s in `:core:presentation:designsystem`. Skill: `figma-to-android-vector`. Verify: each icon renders in a preview, and the path data matches the exported SVG.

## 6. Goals list screen (frames 02–05)

- [ ] 6.1 Create the MVI pieces `GoalsState` (selected tab, loading, per-tab `List<GoalUi>`), `GoalsAction` (tab select, card click, FAB click, back) and `GoalsEvent`, plus `GoalsViewModel(goalRepository: GoalRepository)`. It collects `observeGoals()` and only maps `TrackedGoal` → `GoalUi` (design D6): tab by `status`, sorting per spec ("Goals screen tabs"), and formatting per "Goal card content" ("18.4 / 30 km", "4h 12m / 10h", "3 / 5 runs", "Mar 4 – Mar 10", "N days left", "Ends"/"Ended <date>"). No `RunRepository`, `Clock` or progress logic in the ViewModel. Skills: `android-presentation-mvi`, `kotlin-flows`. Verify: ViewModel unit tests with a fake `GoalRepository` cover tab filtering, sort order, formatting, the loading→content transition, and a goal moving tabs when the fake emits a new status. `./gradlew :goals:presentation:testDebugUnitTest` passes.
- [ ] 6.2 Build `GoalCard` with one `@Preview` each for: Active (Distance), Active (Duration), Active (Frequency), Completed with period ended, Completed with period still running ("Ends …"), and Missed. Skills: `android-compose-components`, `figma-to-android-vector` (icons from 5.1). Verify: each preview matches frames 02/04/05 using `android-preview-figma-verify`.
- [ ] 6.3 Build the `GoalsTabRow` (Active / Completed / Missed chips) with one preview per selected tab, and `GoalsEmptyState` with previews for Active (frame 03), Completed and Missed (placeholder copy, see 9.1). Skill: `android-compose-components`. Verify: `android-preview-figma-verify` against frames 02 and 03.
- [ ] 6.4 Build `GoalsScreen` (top bar with back, tabs, lazy list, FAB) and `GoalsScreenRoot`. Screen previews: Loading, Active populated, Active empty, Completed populated, Completed empty, Missed populated, Missed empty. Skills: `android-compose-architecture`, `android-compose-components`. Verify: `android-preview-figma-verify` against frames 02–05.
- [ ] 6.5 Write instrumented UI tests (real ViewModel, fake `GoalRepository`) for switching tabs, tapping an Active card (navigates to Edit), tapping a Completed card (doesn't navigate), and FAB → New goal. Skill: `android-ui-testing`. Verify: `./gradlew :goals:presentation:connectedDebugAndroidTest` passes.

## 7. New / Edit goal screen (frames 06–08)

- [ ] 7.1 Create `GoalEditorState`/`Action`/`Event` and a `GoalEditorViewModel(goalRepository: GoalRepository, savedStateHandle)` shared by create and edit (mode taken from the optional `goalId` in `SavedStateHandle`). It handles type/period selection (locked in edit), target text through the 3.2 validation, Save enabled only when valid, save/update, the delete dialog, and confirming delete. Skills: `android-presentation-mvi`, `android-error-handling`. Verify: unit tests cover the defaults (Distance, Weekly), unit switching, Save enabled/disabled, edit loading an existing goal with type/period locked, update target, and delete confirm/cancel.
- [ ] 7.2 Build the components `GoalTypeSelector` and `GoalPeriodSelector` (previews: each selection, and locked/disabled), `GoalTargetField` (previews: Distance, Duration, Frequency, empty, with helper text), and `DeleteGoalDialog` (one preview). Skill: `android-compose-components`. Verify: `android-preview-figma-verify` against frames 06, 07 and 08.
- [ ] 7.3 Build `GoalEditorScreen` and `GoalEditorScreenRoot`. Previews: New (Weekly), New (Monthly), New with Save disabled, Edit, and Edit with the delete dialog open. Skills: `android-compose-architecture`, `android-compose-components`. Verify: `android-preview-figma-verify` against frames 06–08.
- [ ] 7.4 Write instrumented UI tests for: creating a goal returns to Goals and it shows up in Active; editing the target; delete → Cancel keeps the goal; delete → Delete removes it; Save is disabled for empty or 0. Skill: `android-ui-testing`. Verify: `./gradlew :goals:presentation:connectedDebugAndroidTest` passes.

## 8. Integration: navigation, Run Overview, logout, DI

- [ ] 8.1 Add `Routes.Goals`, `Routes.NewGoal` and `Routes.EditGoal(goalId)` plus a `goalsGraph` in `NavigationRoot`. Back from Goals returns to Run Overview, and save/delete pops back to Goals (design D8). Skill: `android-navigation`. Verify: E2E test in 8.4.
- [ ] 8.2 Add the "Goals" `DropDownItem` between Analytics and Logout in `RunOverviewScreen` (index mapping 0/1/2), plus `RunOverviewAction.OnGoalsClick` and an `onGoalsClick` callback on `RunOverviewScreenRoot`, and update the Run Overview previews. Skills: `android-compose-components`, `android-presentation-mvi`. Verify: `android-preview-figma-verify` against frame 01 (menu only).
- [ ] 8.3 Switch `RunOverviewViewModel.logout()` to `UserDataCleaner.clearAll()` (design D7), and register the goals Koin modules in `RuniqueApp`. Skills: `android-di-koin`, `android-unit-testing`. Verify: a `RunOverviewViewModel` unit test shows logout calls the cleaner, and the app starts without Koin errors.
- [ ] 8.4 Write an E2E instrumented test (Robot pattern): Run Overview → menu → Goals → create a 30 km Weekly goal → it appears in Active; then logout → login → Goals is empty. Skills: `android-ui-testing`, `android-cli`. Verify: `./gradlew :app:connectedDebugAndroidTest` passes.

## 9. Design follow-ups

- [ ] 9.1 Record in the change (and share with design) the states that have no Figma frame: Completed and Missed empty states, the loading state, Monthly helper text, last-day "days left" copy, "Ends <date>" on a Completed card whose period is still running, and input rejection for invalid targets. Keep placeholder strings in `strings.xml` until frames exist. Verify: the list exists and every placeholder string is marked with a `TODO(design)` comment.

## 10. Final verification

- [ ] 10.1 On a running emulator, verify the live screens against frames 01–08 with `android-ui-screenshot-verify` and `android-ui-verify`, and fix any BLOCKER/MAJOR differences. Skill: `android-cli`. Verify: every frame passes or has documented accepted differences.
- [ ] 10.2 Run every unit and instrumented test of each affected module: `./gradlew :goals:domain:test :goals:data:testDebugUnitTest :goals:presentation:testDebugUnitTest :core:database:testDebugUnitTest :core:domain:test :run:presentation:testDebugUnitTest :app:testDebugUnitTest` and `./gradlew :goals:data:connectedDebugAndroidTest :goals:presentation:connectedDebugAndroidTest :core:database:connectedDebugAndroidTest :run:presentation:connectedDebugAndroidTest :app:connectedDebugAndroidTest`. Verify: all pass.
