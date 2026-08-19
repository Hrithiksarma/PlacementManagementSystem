# Execution Flow — Dashboard Placement Statistics

How a dashboard request and a student edit actually travel through the system today. This is a trace of the real call path (file → function → next file), not an architecture overview — see [`decisions.md`](decisions.md) for why things are shaped this way.

---

## 1. Dashboard data flow (`GET /api/dashboard`)

```
Dashboard.jsx (mount / filter change, debounced 380ms)
    │
    ▼
dashboardService.js  →  getDashboardData(apiParams(filters))
    │  axios.get("http://localhost:8080/api/dashboard", { params: { year, branch, program } })
    ▼
DashboardController.getDashboardData(year, branch, program)
    │  (controller/DashboardController.java — single endpoint, no other routes)
    ▼
DashboardServiceImpl.getDashboardData(year, branch, program)
    │  (service/DashboardServiceImpl.java)
    │
    ├─ 1. Load full tables: studentRepository.findAll(), driveRepository.findAll(),
    │     applicationRepository.findAll(), companyRepository.count()
    │     — no SQL-level aggregation; everything below is in-memory Java Streams.
    │
    ├─ 2. Build filter-option lists (availableYears, availableBranches) — always
    │     unfiltered, for populating the dropdowns.
    │
    ├─ 3. Apply cohort filter (year / branch / program) → `students`.
    │     Derive `applications` by intersecting with `filteredStudentIds`.
    │
    ├─ 4. Compute, in file order:
    │     KPIs (totalStudents, totalPlacedStudents, placementPercentage, …)
    │       → tier distribution (A/B/C/Unplaced counts)
    │       → global package analytics — ALL drives, via packageStats() helper
    │       → drive status counts (upcoming/completed/cancelled)
    │       → eligibility snapshot (no-backlog, dream, super-dream, backlog counts)
    │       → placement trends — ALL years, ALL branches (ignores cohort filter)
    │       → branch-wise placement — cohort-filtered, branch only
    │       → ★ program × branch matrix (NEW) — year-filtered only, ignores the
    │         branch/program dropdowns since those are this table's own pivot
    │         dimensions (see decisions.md)
    │       → top recruiters — top 10 by hire count, cohort-filtered
    │       → ★ full placed-companies list (NEW) — cohort-filtered, uncapped,
    │         alphabetical (built from the same `applications` list as top
    │         recruiters, just without the ranking/limit(10))
    │       → recent activities — latest 10, cohort-filtered
    │       → most-applied company — cohort-filtered
    │       → lastUpdated timestamp
    │
    ▼
Single DashboardDTO, serialized to JSON, one HTTP response.
    │
    ▼
Dashboard.jsx renders each DTO field directly into its section — no client-side
aggregation except small derived "Quick Insight" values (e.g. Students Remaining
= totalStudents - totalPlacedStudents).
```

### Program × branch matrix, in detail (`DashboardServiceImpl`)

```
allStudents
  → filter by `year` only               (yearPool)
  → group by department.program         (byProgram: "B.Tech" → [...], "M.Tech" → [...])
      → group by department.branch      (byBranchInProgram: "CSE" → [...], "ECE" → [...])
          → computeCategoryStats(branch, group, allApplications)   per branch
      → computeCategoryStats("Overall", programStudents, allApplications)  synthesized rollup
  → ProgramStatsDTO { program, categories: [CSE, ECE, ..., Overall] }
```

`computeCategoryStats(label, group, allApplications)` — the shared per-cell calculation:
1. `totalRegistered` = `group.size()`
2. `optedForHigherStudies` = count of `group` where `Student.optedForHigherStudies == true`
3. Filter `allApplications` down to this group's students with `status == "Selected"` → `groupSelectedApps`
4. `totalJobsOffered` = `groupSelectedApps.size()` (application-level count, can exceed step 5)
5. `totalSelected` = count of `group` where `placementTier` is set and not `"Unplaced"` (student-level count)
6. `percentagePlaced` = `totalSelected / totalRegistered * 100`
7. For each student in `groupSelectedApps`, take the **max** `Drive.packageLpa` across their selected applications → one salary value per placed student → run through `packageStats()` → `highestSalary` / `averageSalary` / `medianSalary`

`packageStats(values)` is the same helper used for the pre-existing global package KPI (extracted from what used to be inline duplicated logic) — sorts, then returns `{highest, average, median}`, all rounded to 1 decimal, `{0,0,0}` if the list is empty.

---

## 2. Student edit flow (`PUT /students/{id}`) — how `optedForHigherStudies` gets set

```
Students.jsx (admin clicks "Edit" on a student → checkbox toggle → "Save")
    │  handleSave() builds a payload: { ...student, ...form fields, optedForHigherStudies }
    ▼
studentService.js  →  updateStudent(id, payload)
    │  axios.put(`${API_URL}/${id}`, payload)
    ▼
StudentController.updateStudent(id, @RequestBody Student student)
    │  (controller/StudentController.java — takes the raw `Student` ENTITY as the
    │   request body; there is no StudentDTO translation layer for this endpoint)
    ▼
StudentServiceImpl.updateStudent(id, student)
    │  (service/StudentServiceImpl.java)
    ▼
StudentRepository.save(student)
    → Hibernate UPDATE ... SET opted_for_higher_studies = ?, ... WHERE student_id = ?
```

Because `StudentController` accepts the entity directly (no DTO), adding `optedForHigherStudies` to `Student.java` was sufficient on its own — no controller, service, or repository changes were needed to make the field persist. The column itself is created automatically by Hibernate on next app start (`spring.jpa.hibernate.ddl-auto=update` in `application.properties`) — no manual migration.

---

## 3. Where each new UI element reads its data

| UI element | Source field on `DashboardDTO` | Rendered in |
|---|---|---|
| "Placement Statistics by Program" tables | `programStats[].categories[]` | `Dashboard.jsx` → `ProgramStatsTable` |
| "Companies Placed" chip list | `placedCompanies[]` | `Dashboard.jsx` (inline) |
| "Higher Studies" Yes/No (read-only) | `student.optedForHigherStudies` | `Students.jsx` (`field(...)` block) |
| "Opted for Higher Studies" checkbox (edit) | `form.optedForHigherStudies` | `Students.jsx` (edit form) |
