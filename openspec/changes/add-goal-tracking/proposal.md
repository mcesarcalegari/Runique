# Proposal

## Why

Runique records runs but gives runners nothing to aim for. Goal tracking lets a user set a distance, duration or frequency target for the current week or month and watch their logged runs fill it. The full set of Figma designs for this feature is ready, so the behaviour can now be pinned down and built.

## What Changes

- New **Goals** entry in the Run Overview overflow menu, between "Analytics" and "Logout" ([01 — Run Overview menu open](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4012-1520)). Only the menu item is in scope; the "Past Runs" year/month filter and sort control visible in that frame are background, not part of this change.
- New **Goals** screen with three tabs — Active, Completed, Missed — and a FAB to create a goal ([02](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4011-1520), [03](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4013-1520), [04](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4014-1520), [05](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4015-1520)).
- New **New goal** screen: choose type (Distance / Duration / Frequency), a target and a period (Weekly / Monthly) ([06](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4016-1520)).
- New **Edit goal** screen for Active goals: only the target can change; type and period are locked. Includes deleting a goal behind a confirmation dialog ([07](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4017-1520), [08](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4018-1520)).
- Goals are **one-time**: each covers exactly one calendar week (Mon 00:00 – Sun 23:59 local) or calendar month, always the period in which it was created, counting every run in that period, including runs before the goal was created.
- Goal lifecycle: **Active** while under 100% and the period is running; moves to **Completed** the moment progress reaches 100% (and keeps accumulating, e.g. 107%, until the period ends); becomes **Missed** when the period ends under 100%. Completion is permanent once reached.
- Goals are stored locally in the existing Room database (schema version 1 → 2, with a migration) and are cleared on logout together with runs. No backend or sync.

## Capabilities

### New Capabilities
- `goal-tracking`: Creating, editing and deleting one-time running goals, computing their progress from logged runs, the Active / Completed / Missed lifecycle, and how goals are presented and reached from the app.

### Modified Capabilities
<!-- None: no specs exist yet in openspec/specs/. -->

## Impact

- **New modules**: `:goals:domain`, `:goals:data`, `:goals:presentation` (mirroring `run/*`).
- **`:core:database`**: new `GoalEntity` + `GoalDao`, `RunDatabase` version bumped to 2 with a manual `Migration(1, 2)`.
- **`:core:domain`**: a small user-data cleaner used on logout so runs and goals are cleared without coupling features.
- **`:run:presentation`**: Run Overview menu gets a "Goals" item and a new navigation callback; logout also clears goals.
- **`:app`**: new routes and nav-graph entries for Goals, New goal and Edit goal; Koin module wiring.
- **`:core:presentation:designsystem`**: new icons taken from Figma (goal target, distance, duration, frequency, calendar, clock, flag, lock, trash, check) where not already present.
- **Design follow-ups (no Figma frame yet)**: empty states for Completed/Missed, loading state, Monthly helper text, last-day copy, invalid-target state, "Ends <date>" label for goals completed while their period is still running. These are flagged, not invented.
- No API, backend or Wear OS changes.
