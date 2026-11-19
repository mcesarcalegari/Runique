# goal-tracking Specification

## Purpose

Lets a runner set one-time distance, duration or frequency targets for the current calendar week or month, and track progress toward them from the runs they log, with each goal ending as Completed or Missed.

## Requirements

### Requirement: Goals entry point
The Run Overview overflow menu SHALL contain a "Goals" item between "Analytics" and "Logout" that opens the Goals screen on the Active tab. UI: [01 — Run Overview menu open](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4012-1520).

#### Scenario: Open goals from the menu
- **WHEN** the user opens the Run Overview menu and taps "Goals"
- **THEN** the Goals screen opens with the Active tab selected

#### Scenario: Menu order
- **WHEN** the user opens the Run Overview menu
- **THEN** the items are shown in the order Analytics, Goals, Logout

### Requirement: Goal definition
A goal SHALL have a type (Distance in km, Duration in hours, or Frequency in runs), a positive target and a period (Weekly or Monthly). Each goal is one-time: it covers exactly one period and is never renewed automatically.

#### Scenario: One-time goal does not renew
- **GIVEN** a Weekly Distance goal for Mon 4 Mar – Sun 10 Mar
- **WHEN** Monday 11 March begins
- **THEN** no new goal is created for the week of 11 March

### Requirement: Goal period window
A goal's period SHALL be the calendar period in which it is created, in the device's local time zone: Monday 00:00 to Sunday 23:59:59 for Weekly, and the 1st 00:00 to the last day 23:59:59 for Monthly. Rolling windows (such as the last 7 days) SHALL NOT be used.

#### Scenario: Weekly goal created mid-week
- **WHEN** the user creates a Weekly goal on Thursday 7 March 2024
- **THEN** the goal's period is Monday 4 March 00:00 to Sunday 10 March 23:59:59

#### Scenario: Monthly goal created mid-month
- **WHEN** the user creates a Monthly goal on 7 March 2024
- **THEN** the goal's period is 1 March 00:00 to 31 March 23:59:59

#### Scenario: Monthly goal in a leap-year February
- **WHEN** the user creates a Monthly goal on 10 February 2024
- **THEN** the goal's period ends on 29 February 23:59:59

### Requirement: Progress calculation
A goal's progress SHALL be computed from every run whose local start time falls within the goal's period, including runs logged before the goal was created. Distance sums run distances, Duration sums run durations, Frequency counts runs. A run is assigned to the period containing its start time.

#### Scenario: Earlier runs in the period count
- **GIVEN** the user ran 5 km on Monday 4 March
- **WHEN** they create a Weekly Distance goal on Thursday 7 March
- **THEN** the goal starts with 5 km of progress

#### Scenario: Runs outside the period are ignored
- **GIVEN** a Weekly goal for 4–10 March
- **WHEN** the user has a run on Sunday 3 March
- **THEN** that run does not contribute to the goal

#### Scenario: Run crossing midnight
- **GIVEN** a run that starts Sunday 10 March 23:40 and ends Monday 11 March 00:20
- **WHEN** progress is computed
- **THEN** the whole run counts toward the week of 4–10 March only

#### Scenario: Frequency counts runs
- **WHEN** three runs fall within a Frequency goal's period
- **THEN** the goal's progress is 3 runs

### Requirement: Progress percentage
The progress percentage SHALL be progress divided by target, multiplied by 100 and rounded down to a whole number. It is not capped, so Completed goals can show values above 100%.

#### Scenario: Rounding down
- **WHEN** a goal has 18.4 km of a 30 km target
- **THEN** the percentage shown is 61%

#### Scenario: Above target
- **WHEN** a Completed goal has 32.1 km of a 30 km target
- **THEN** the percentage shown is 107%

#### Scenario: Just below target
- **WHEN** a goal has 29.9 km of a 30 km target
- **THEN** the percentage shown is 99% and the goal is still Active

### Requirement: Goal lifecycle
A goal SHALL be Active while its period is running and progress is below target. It SHALL become Completed as soon as progress reaches or exceeds target, even mid-period, and Missed when its period ends with progress below target.

#### Scenario: Reaching the target mid-period
- **GIVEN** an Active Weekly Distance goal of 30 km with 28 km of progress
- **WHEN** a 3 km run inside the period is saved
- **THEN** the goal moves to the Completed tab showing 103%

#### Scenario: Completed goal keeps accumulating
- **GIVEN** a goal that became Completed while its period is still running
- **WHEN** another run inside the period is saved
- **THEN** the goal's progress and percentage increase, and it stays Completed

#### Scenario: Period ends below target
- **GIVEN** an Active goal at 75% on the last day of its period
- **WHEN** the period ends
- **THEN** the goal appears in the Missed tab showing 75%

#### Scenario: Active never shows 100% or more
- **WHEN** the Active tab is shown
- **THEN** every goal in it has a percentage below 100%

### Requirement: Completion is permanent
Once a goal has reached its target, it SHALL remain Completed even if contributing runs are later deleted, and its period ending SHALL NOT move it to Missed.

#### Scenario: Deleting a contributing run after completion
- **GIVEN** a Completed 30 km goal with 31 km of progress
- **WHEN** the user deletes a 5 km run within the goal's period
- **THEN** the goal shows 26 km and 86% and stays in the Completed tab

### Requirement: Goals screen tabs
The Goals screen SHALL show Active, Completed and Missed tabs, each listing only goals in that state (Active by period end ascending, others descending), and a FAB opening New goal. UI: [02](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4011-1520), [04](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4014-1520), [05](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4015-1520).

#### Scenario: Switching tabs
- **WHEN** the user selects the Missed tab
- **THEN** only Missed goals are listed, most recent period first

#### Scenario: FAB from any tab
- **WHEN** the user taps the FAB on the Completed tab
- **THEN** the New goal screen opens

#### Scenario: Back navigation
- **WHEN** the user taps back on the Goals screen
- **THEN** they return to Run Overview

### Requirement: Goal card content
Each goal card SHALL show a type icon, a title "<Weekly|Monthly> <Distance|Duration|Frequency>", progress and target with unit, the percentage, a progress bar (filled to at most 100% visually), the period date range, and a status line. Distance uses one decimal ("18.4 / 30 km"), Duration uses hours and minutes ("4h 12m / 10h"), Frequency uses runs ("3 / 5 runs").

#### Scenario: Active card status line
- **WHEN** an Active goal's period ends on 10 March and today is 7 March
- **THEN** the card shows "3 days left" and the range "Mar 4 – Mar 10"

#### Scenario: Completed card badge and styling
- **WHEN** a goal is Completed
- **THEN** its card shows a green "Completed" badge and a green progress bar, as in frame 04

#### Scenario: Missed card badge and styling
- **WHEN** a goal is Missed
- **THEN** its card shows a grey "Missed" badge, a grey progress bar, a grey percentage and a muted icon, as in frame 05

#### Scenario: Ended date label
- **WHEN** a Completed or Missed goal's period has ended on 3 March
- **THEN** the card shows "Ended Mar 3"

#### Scenario: Completed while period is still running
- **WHEN** a goal is Completed and its period ends on 10 March, which is in the future
- **THEN** the card shows "Ends Mar 10" instead of "Ended"

### Requirement: Empty and loading states
Each tab SHALL show an empty state when it has no goals and a loading state until goals are first loaded. The Active empty state follows frame 03 ("No active goals" / "Tap + to start tracking one"); Completed and Missed empty states and the loading state follow their Figma frames once those are designed. UI: [03](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4013-1520).

#### Scenario: No active goals
- **WHEN** the user has no Active goals and opens the Active tab
- **THEN** the empty state with the target icon, "No active goals" and "Tap + to start tracking one" is shown

#### Scenario: No missed goals
- **WHEN** the user has no Missed goals and opens the Missed tab
- **THEN** an empty state is shown instead of a blank list

### Requirement: Create a goal
The New goal screen SHALL let the user pick a type (default Distance), enter a target and pick a period (default Weekly), then save. The target field shows the unit for the selected type and a helper text describing the type and period. Saving creates the goal and returns to the Goals screen. UI: [06](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4016-1520).

#### Scenario: Create a weekly distance goal
- **WHEN** the user picks Distance, enters 30, picks Weekly and taps "Save goal"
- **THEN** a 30 km Weekly Distance goal for the current week is created and the user is back on the Goals screen

#### Scenario: Unit follows type
- **WHEN** the user switches the type from Distance to Frequency
- **THEN** the target field's unit changes from "km" to "runs"

#### Scenario: Weekly helper text
- **WHEN** Weekly is selected
- **THEN** the helper text "Starts Monday 00:00 local, ends Sunday 23:59." is shown under the period selector

#### Scenario: Goal reaches target on creation
- **GIVEN** the user already ran 32 km this week
- **WHEN** they create a 30 km Weekly Distance goal
- **THEN** the goal is immediately Completed

### Requirement: Target validation
The target SHALL be greater than zero. Distance accepts up to one decimal place; Duration (hours) and Frequency (runs) accept whole numbers only. Save SHALL be disabled while the target is empty or invalid.

#### Scenario: Empty target
- **WHEN** the target field is empty
- **THEN** "Save goal" is disabled

#### Scenario: Zero target
- **WHEN** the user enters 0
- **THEN** "Save goal" is disabled

#### Scenario: Decimal distance
- **WHEN** Distance is selected and the user enters 12.5
- **THEN** "Save goal" is enabled

#### Scenario: Decimal frequency rejected
- **WHEN** Frequency is selected and the user enters 2.5
- **THEN** the value is not accepted and "Save goal" is not enabled for it

### Requirement: Edit an active goal
Tapping an Active goal card SHALL open Edit goal, where only the target can change. Type and period are shown locked with "Type can't be changed after creation" and "Period and dates stay the same". Completed and Missed goals SHALL NOT be editable. UI: [07](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4017-1520).

#### Scenario: Change target
- **WHEN** the user changes an Active goal's target from 30 to 40 km and taps "Save changes"
- **THEN** the goal keeps its type and period, its target is 40 km, and the user is back on the Goals screen

#### Scenario: Lowering target below progress
- **GIVEN** an Active goal with 18.4 km of a 40 km target
- **WHEN** the user changes the target to 15 km and saves
- **THEN** the goal becomes Completed showing 122%

#### Scenario: Completed goals are not editable
- **WHEN** the user taps a card in the Completed or Missed tab
- **THEN** no edit screen opens

### Requirement: Delete an active goal
Edit goal SHALL offer "Delete goal", which asks for confirmation ("Delete this goal?" / "This can't be undone. Runs that contributed to this goal stay in your history."). Confirming deletes the goal only, never runs. UI: [08](https://www.figma.com/design/bcIkmnAUfjg4WcR5qQUR2Y/Runique---Mauro-Team?node-id=4018-1520).

#### Scenario: Confirm delete
- **WHEN** the user taps "Delete goal" then "Delete"
- **THEN** the goal is removed, the user is back on the Goals screen, and all runs are unchanged

#### Scenario: Cancel delete
- **WHEN** the user taps "Delete goal" then "Cancel"
- **THEN** the dialog closes and the goal is unchanged

### Requirement: Multiple goals
The user SHALL be able to have several Active goals at once, including more than one with the same type and period.

#### Scenario: Duplicate type and period
- **GIVEN** an Active Weekly Distance goal
- **WHEN** the user creates another Weekly Distance goal
- **THEN** both goals are listed in the Active tab

### Requirement: Goal storage and logout
Goals SHALL be stored on the device only and survive app restarts. Logging out SHALL delete all goals along with runs.

#### Scenario: Survives restart
- **WHEN** the user creates a goal and restarts the app
- **THEN** the goal is still listed

#### Scenario: Logout clears goals
- **WHEN** the user logs out and logs back in
- **THEN** no goals are listed
