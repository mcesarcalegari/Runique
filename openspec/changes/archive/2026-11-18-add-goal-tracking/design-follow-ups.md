# Design follow-ups (no Figma frame yet)

These states are built with placeholder copy or visuals so the feature works. Every placeholder
string in `goals/presentation/src/main/res/values/strings.xml` is marked `TODO(design)`. Replace
them once frames exist in the Runique Figma file (`bcIkmnAUfjg4WcR5qQUR2Y`).

| State | Where | Current placeholder |
|---|---|---|
| Completed tab empty state | `GoalsEmptyState` (COMPLETED) | Frame 03 layout, check icon, "No completed goals" / "Goals you reach show up here" |
| Missed tab empty state | `GoalsEmptyState` (MISSED) | Frame 03 layout, flag icon, "No missed goals" / "Goals that end below target show up here" |
| Loading state (Goals list, Edit goal) | `GoalsScreen`, `GoalEditorScreen` | Centered Material `CircularProgressIndicator` |
| Monthly helper text | `GoalPeriodSelector` | "Starts on the 1st 00:00 local, ends on the last day 23:59." |
| Duration / Frequency helper text (New goal) | `GoalEditorScreen` | "Total running time to reach during the period." / "Number of runs to complete during the period." |
| Duration unit label in the target field | `GoalTargetField` | "hours" |
| Empty target field hint | `GoalTargetField` | "0" at 40% opacity |
| Last-day "days left" copy | `GoalCard` footer (`DaysLeft(0)`) | Clock icon + "Last day" |
| Completed card whose period is still running | `GoalCard` footer (`Ends`) | Flag icon + "Ends Mar 10" |
| Input rejection for invalid targets | `GoalEditorViewModel` | Invalid keystrokes (2nd decimal, decimals for hours/runs, >6 digits) are silently ignored; no error message |

## Deviations from the frames (by decision, not missing design)

Per the decision on 2026-11-18 to reuse the existing design system as-is:

- Colors use the existing tokens: card `#232624` (Figma `#262221`), muted `#87938C`
  (Figma `#96908C`), red `#BB3D3D` (Figma `#FF3B30`).
- Top bar is `RuniqueToolbar` (long-arrow back icon, SemiBold title) instead of the Figma chevron
  plus Bold 18sp title.
- Delete dialog is `RuniqueDialog` (centered title and description, 15dp padding/radius). Only the
  Cancel and Delete buttons follow frame 08.
- Save button is `RuniqueActionButton`, slightly taller than the 48dp in frames 06/07.
- Calendar, lock and check glyphs reuse the existing design-system icons, which differ slightly
  from the Figma glyphs.
- Run Overview menu (frame 01): only the "Goals" item and its target icon were added; the existing
  `DropdownMenu` styling and the Analytics/Logout icons are unchanged.
- Unselected tab-chip border uses 40% opacity (frames 03–05). Frame 02 uses 70%.

## Live verification (task 10.1, 2026-11-18, Pixel_10 emulator, 420dpi)

Checked with annotated screenshots (`android screen capture --annotate`) and the UI Automator tree
(`android layout`, ±4dp) against the Figma structure from `get_design_context`.

| Frame | Result | Notes |
|---|---|---|
| 01 Run Overview menu | Pass (in scope) | Order Analytics, Goals, Logout; Goals uses the Figma target icon. Menu container padding, row height and scrim come from the existing `DropdownMenu` (out of scope). |
| 02 Goals Active populated | Pass | Chips 70/99/73.5dp (Figma 71/101/75), 8dp gaps; card 148.6dp tall (Figma 151), 16dp below chips; period and days-left correct for the current week. |
| 03 Goals Active empty | Pass | Empty state centred above the FAB area; 8dp title/description gap. |
| 04 Goals Completed | Accepted, previews only | Not reachable live (goals are always created in the current period). Approved by the user. |
| 05 Goals Missed | Accepted, previews only | Same as 04. Approved by the user. |
| 06 New goal | Pass after fix | MAJOR fixed: the keyboard covered "Save goal". Added `imePadding()` to the editor so the actions stay above the IME. |
| 07 Edit goal | Pass | Locked selectors at 60% with lock hints; 12dp between Save and the 48dp Delete button. |
| 08 Delete dialog | Pass with accepted differences | Exact copy, end-aligned Cancel + red Delete pill. 16dp button gap (Figma 12) and centred title from `RuniqueDialog`. |

Accepted differences everywhere: the reuse-as-is items listed above, plus Save at 56dp (Figma 48).
