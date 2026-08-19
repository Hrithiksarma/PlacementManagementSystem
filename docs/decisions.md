# Decisions Log — Dashboard Placement Statistics (Program × Branch)

Running log of meaningful decisions for the "show full IIITG-style placement stats on the Dashboard" feature (company list, B.Tech/M.Tech × CSE/ECE breakdown table, real salary stats). Each entry records what was decided, the alternative(s) considered, and why — not a design doc, a decision log. Append new entries as they come up; don't rewrite history.

---

## 2026-08-12 — Higher-studies tracking: new field vs. new tier value

**Decision:** Add a new `optedForHigherStudies` boolean field on `Student` (`entity/Student.java`), editable via a checkbox in the existing admin "Edit Student" form (`Students.jsx`) — the same manual-edit pattern already used for `placementTier`.

**Alternative considered:** Fold it into `placementTier` as a 5th value (`"Higher Studies"`) alongside `A`/`B`/`C`/`Unplaced`, avoiding a new column.

**Why:** "Opted out of placement for higher studies" and "placement outcome" are different axes — a student can (in principle) opt for higher studies and still separately hold a placement tier for the purposes of the dashboard, and conflating the two would have forced changes to the existing tier-distribution pie chart and its unplaced-count logic. A separate boolean keeps both existing logic and the new logic untouched.

**Confirmed by:** explicit user choice between the two options (asked via AskUserQuestion during planning).

---

## 2026-08-12 — Salary stats basis: actual placed students vs. all drives

**Decision:** The new per-category `averageSalary` / `medianSalary` / `highestSalary` (in `DashboardServiceImpl.computeCategoryStats`) are computed from **actually-placed students' accepted (`"Selected"`) `Application` → `Drive.packageLpa`** — not from every `Drive` row that exists.

**Alternative considered:** Reuse the existing global `averagePackage`/`highestPackage`/`medianPackage` computation basis (all `Drive.packageLpa` values, regardless of whether anyone was hired).

**Why:** The real IIITG site's numbers only make sense as "salary actually won by placed students" — a drive that nobody applied to or was selected for shouldn't move the reported average/median/highest salary for a branch. The existing global KPI (`averagePackage` etc.) is left exactly as-is (all-drives basis) for backward compatibility — it answers a different question ("how well do drives on our platform pay") than the new per-category numbers ("what did our placed students actually earn").

---

## 2026-08-12 — "Jobs offered" vs. "students selected" as two distinct counts

**Decision:** `totalJobsOffered` = count of `Application` rows with `status == "Selected"` (an application-level count). `totalSelected` = count of distinct students whose `placementTier` is not null/`"Unplaced"` (a student-level count). These can differ.

**Alternative considered:** Treat them as the same number (one placed student = one job).

**Why:** The real site's own CSE row shows 133 jobs offered vs. 116 students selected — i.e., some students hold multiple offers. Modeling these as two separate counts is the only way to reproduce that distinction faithfully instead of just repeating the same number twice.

**Follow-on decision:** When computing a placed student's salary for the average/median/highest calculation, use their **best** (`max`) package among all their `"Selected"` applications, on the theory that a student with multiple offers is credited for the one they'd realistically report. This is a judgment call — the data model has no explicit "accepted offer" flag distinct from `Application.status == "Selected"`; `offerAcceptedAt` exists on `Application` but wasn't used here to keep the aggregation logic simple and avoid a second filtering pass with unclear fallback semantics if `offerAcceptedAt` is null on an otherwise-selected application.

---

## 2026-08-12 — Program × branch matrix filter scope

**Decision:** The new program/branch statistics matrix is filtered by the dashboard's `year` dropdown only. It ignores the dashboard's own `branch` and `program` dropdowns.

**Alternative considered:** Apply all three dashboard filters (`year`, `branch`, `program`) the same way the rest of the dashboard does.

**Why:** Branch and program are themselves the row/column pivot dimensions of this table — filtering the *inputs* to those same dimensions would mean, e.g., selecting "CSE" in the branch dropdown makes the CSE column collapse to trivially 100%-consistent data or disappear entirely, which isn't a sensible reading of "show me all categories." The real site presents this as one fixed snapshot table, not a filterable one (beyond the academic year it's already scoped to).

---

## 2026-08-12 — Documentation format

**Decision:** Track decisions here as they're made, in dated, alternative-and-rationale format, rather than only in commit messages or PR descriptions.

**Why:** Requested explicitly, to make the reasoning behind non-obvious modeling choices (the four above) discoverable later without having to reconstruct them from the diff. See [`flow.md`](flow.md) for the complementary execution-trace document.
