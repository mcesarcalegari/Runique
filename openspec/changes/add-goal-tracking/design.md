# Design

## Context

See proposal.md for motivation and specs/goal-tracking/spec.md for the behaviour contract. The parts of the current codebase that shape this design:

- Runs live in a single Room database `RunDatabase` (`:core:database`, `version = 1`, built in `DatabaseModule` without migrations). The Room convention plugin exports schemas to `$projectDir/schemas`, but no `1.json` is committed.
- `RunRepository.getRuns(): Flow<List<Run>>` (`:core:domain`) emits all locally stored runs, including runs synced from the backend and the Wear app. `Run.dateTimeUtc` is the run's start time; `distanceMeters` and `duration` are available.
- Logout is done in `RunOverviewViewModel.logout()`, which calls `runRepository.deleteAllRuns()` and then `runRepository.logout()`.
- Navigation is type-safe Compose navigation in `:app` (`Routes`, `NavigationRoot.runGraph`). The overview menu is a `DropDownItem` list in `RunOverviewScreen`, indexed by position.
- `RuniqueApp` exposes an app-wide `applicationScope`.
- Feature modules follow `<feature>:domain|data|presentation`. Analytics is a dynamic feature; goals don't need to be one.

## Goals / Non-Goals

**Goals:**
- Progress always reflects the current local run list, including runs added, synced or deleted while the Goals screen is closed.
- All status and progress rules (period window, percentage, lifecycle, latch) live behind `GoalRepository`, in pure, clock-injected functions in `:goals:data` that can be unit tested. ViewModels only map repository output to UI.
- One Room migration with an instrumented migration test.

**Non-Goals:**
- Backend storage, sync or multi-device goals.
- Recurring goals, notifications, or Wear OS goal UI.
- Imperial units (the app is km-only today).
- Any Run Overview change other than the "Goals" menu item. The filter and sort controls in frame 01 are not built.

## Decisions

### D1. Module layout: `:goals:domain`, `:goals:data`, `:goals:presentation`
Same structure as `run/*`. `:goals:domain` holds the models (`Goal`, `GoalType`, `GoalPeriod`, `GoalStatus`, `TrackedGoal`), the `GoalRepository` interface and the target validator. There are no use-case classes; the project doesn't use them. `:goals:data` implements the repository on top of `:core:database` and `RunRepository`, and holds all progress logic. `:goals:presentation` holds the screens; its ViewModels get only the `GoalRepository` interface injected.
*Alternative:* add goals to the `run/*` modules. Rejected: goals are a separate feature with their own screens, and this keeps `run` focused.

### D2. Store the goal definition and completion time, compute everything else
`GoalEntity` (in `:core:database`, added to `RunDatabase`) stores:

| field | notes |
|---|---|
| `id: String` | ObjectId hex, same as `RunEntity` |
| `type: String` | `DISTANCE` / `DURATION` / `FREQUENCY` |
| `period: String` | `WEEKLY` / `MONTHLY` |
| `target: Long` | in base units: metres (Distance; 12.5 km → 12 500), seconds (Duration; whole hours × 3600), runs (Frequency) |
| `periodStartDate: String` | ISO `LocalDate` (Monday or the 1st) |
| `periodEndDate: String` | ISO `LocalDate` (Sunday or the last day of the month) |
| `createdAtUtc: String` | ISO instant |
| `completedAtUtc: String?` | set once, when progress first reaches target |

Progress, percentage, days left and Active vs Missed are never stored. They come from runs and the clock. Storing integer base units avoids floating-point comparisons with target values in km.
*Alternative:* store progress and status. Rejected: they go stale whenever runs change while the screen is closed.

### D3. Period stored as local dates, evaluated in the device's current zone
The period is fixed at creation as a `LocalDate` range from `today` in the zone at that time (`TemporalAdjusters.previousOrSame(MONDAY)` / `firstDayOfMonth()`, with matching ends). A run counts if `run.dateTimeUtc` converted to `ZoneId.systemDefault()` has a local date within `[start, end]`. The range a goal covers never moves. Only which runs fall into it can shift if the user changes time zone, and this is accepted.
*Alternative:* store UTC instants for the bounds. Rejected: "Monday 00:00 local" then becomes wrong after a time zone or DST change.

### D4. Repository exposes goals with progress already computed
`GoalRepository.observeGoals(): Flow<List<TrackedGoal>>` is the only way to read goals. `TrackedGoal` has the goal plus `progress`, `percent`, `status` and `daysLeft`. `RoomGoalRepository` combines `goalDao.observeAll()`, `runRepository.getRuns()` and a minute ticker driven by an injected `java.time.Clock`. The ticker means status and "days left" update at midnight or when a period ends while someone is watching.

The calculation itself is `internal` pure functions in `:goals:data` (`GoalProgressCalculator`) over `(Goal, List<Run>, now: ZonedDateTime)`, so it can be unit tested without Room:
- `progress`: sum of metres or seconds, or a run count.
- `percent = floor(progress * 100 / target)`, computed in `Long`.
- `status`:
  - `completedAt != null` → Completed;
  - otherwise `progress >= target` → Completed (the latch is about to be written);
  - otherwise today after `periodEnd` → Missed;
  - otherwise Active.
- `daysLeft = ChronoUnit.DAYS.between(today, periodEnd)`.

A `java.time.Clock` is provided through Koin so tests can fix "now" (for example Thu 7 Mar 2024).
*Alternative:* the ViewModel combines goals, runs and the clock itself, or calls an `ObserveGoalProgress` use case. Rejected: the project doesn't use use cases, and the ViewModel would need `RunRepository` and `Clock` only to recompute data-layer facts.

### D5. Completion latch runs in the app scope
`GoalCompletionObserver` in `:goals:data` combines `goalDao.observeAll()` with `runRepository.getRuns()`. For every goal whose `completedAtUtc` is null, whose period hasn't ended, and whose progress is at or above target, it writes `completedAtUtc = now`. It is started once from `RuniqueApp.applicationScope`, so completion is recorded even when a run is saved with the Goals screen closed. The repository also checks the latch directly after creating or editing a goal, which covers "target reached on creation" and "target lowered below progress".
*Alternative:* write the latch only from the Goals ViewModel. Rejected: a run saved and then deleted while the screen is closed would leave the goal un-latched, breaking "completion is permanent".
*Edge:* the observer only latches goals whose period is still running. A goal that ends below target is Missed for good. Runs synced later from another device into an ended period still change the displayed progress, but not the status.

### D6. Reactive screen state
`GoalsViewModel(goalRepository: GoalRepository)` collects `observeGoals()` and maps each `TrackedGoal` to a `GoalUi`, putting each goal in its tab by `status` and sorting as the spec requires. It does no progress or time calculation itself. Loading is the initial state until the first emission. `GoalEditorViewModel(goalRepository: GoalRepository, savedStateHandle)` uses the same interface to load, create, update and delete goals.

### D7. Logout clears goals through a cross-feature cleaner, not a feature dependency
`:run:presentation` must not depend on `:goals:*`. Add `UserDataCleaner { suspend fun clearAll() }` to `:core:domain`, implemented in `:core:database` with a Room `withTransaction` that deletes runs and goals, which keeps today's behaviour for every other table. `RunOverviewViewModel.logout()` calls it instead of `runRepository.deleteAllRuns()`.
*Alternative:* `run:presentation` depends on `goals:domain`. Rejected because it couples two features. *Alternative:* `RunDatabase.clearAllTables()`. Rejected: it would also wipe the pending-sync tables, which logout doesn't do today.

### D8. Navigation
Add `Routes.Goals`, `Routes.NewGoal` and `@Serializable data class Routes.EditGoal(val goalId: String)` to `:app`, inside a `goalsGraph` registered next to `runGraph`. `RunOverviewScreenRoot` gets an `onGoalsClick` callback, and the menu becomes `[Analytics, Goals, Logout]` with index mapping 0/1/2. Edit receives `goalId` through `SavedStateHandle.toRoute()`. Saving or deleting pops back to Goals.

### D9. Target input
- Distance: a decimal text field limited to one decimal place, parsed into metres.
- Duration: whole hours.
- Frequency: whole runs.

Input that breaks these rules is filtered out at the keyboard or text-field level, with no error message (none is designed). Save is enabled only when the parsed value is greater than 0. Duration progress is shown as `Xh Ym`; whole-hour targets as `10h`.

### D10. Room migration 1 → 2
`RunDatabase.version = 2`, with a manual `Migration(1, 2)` that runs `CREATE TABLE GoalEntity (...)`. AutoMigration needs `1.json`, which isn't committed. As a first step, generate and commit `core/database/schemas/.../1.json` from the unchanged schema, then add `2.json`, so `MigrationTestHelper` can verify the migration. Register the migration in `DatabaseModule`.

## Risks / Trade-offs

- [All runs are loaded into memory for each progress calculation] → The run count is small (personal history), and `getRuns()` is already observed by Run Overview. If it grows, add a DAO query by date range later; the behaviour doesn't change.
- [Changing time zone moves runs between periods] → Accepted and documented in D3. It only affects runs near midnight.
- [The completion observer runs for the whole process lifetime] → It's cheap: it combines two Room flows and only writes when a latch changes.
- [Undesigned UI: Completed/Missed empty states, loading, Monthly helper text, last-day copy, "Ends <date>"] → Build them with placeholder strings from the existing design system and track them as design follow-ups (task group 9). Replace them once Figma frames exist.
- [Missing `1.json` schema] → Task 2.1 generates it from the current master before the version bump.

## Migration Plan

1. Ship DB version 2 with `Migration(1, 2)`. Existing users keep their runs, and the goals table starts empty.
2. Rollback means removing the feature in a later version. Room downgrades aren't supported, so a rollback build must keep version ≥ 2 (and can leave the table unused).

## Open Questions

- Final copy and visuals for the design follow-ups listed under Risks. They don't change the specs or the structure.
