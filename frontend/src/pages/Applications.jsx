import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import {
  getFilteredApplications,
  updateApplicationStatus,
  deleteApplication,
} from "../services/applicationService";
import { getStudents } from "../services/studentService";
import { getAllDrives } from "../services/driveService";
import "./Applications.css";

// ─── Constants ───────────────────────────────────────────────────────────────

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

const DEPT_BY_PROGRAM = {
  "B.Tech": ["CSE", "ECE"],
  "M.Tech": ["CSE", "ECE"],
  "PhD":    ["CSE", "ECE", "Science & Mathematics", "HSS"],
};

const BATCH_YEARS = [];
for (let y = 2015; y <= 2030; y++) BATCH_YEARS.push(y);

// Filters panel — all possible statuses
const STATUS_OPTIONS = [
  "Applied", "Shortlisted", "Interview Scheduled",
  "Selected", "Offer Released", "Offer Accepted", "Offer Rejected",
  "Rejected", "Withdrawn",
];

// Admin-editable transitions (Offer Accepted / Rejected are student-only)
const STATUS_TRANSITIONS = {
  "Applied":             ["Shortlisted", "Rejected"],
  "Shortlisted":         ["Interview Scheduled", "Rejected"],
  "Interview Scheduled": ["Selected", "Rejected"],
  "Selected":            ["Offer Released", "Rejected"],
};

const STATUS_SELECT_STYLE = {
  Applied:               { background: "#eff6ff", color: "#1d4ed8" },
  Shortlisted:           { background: "#f5f3ff", color: "#6d28d9" },
  "Interview Scheduled": { background: "#fffbeb", color: "#92400e" },
  Selected:              { background: "#fefce8", color: "#92400e" },
};

const FINAL_STATUS_STYLE = {
  "Offer Released": { background: "#fffbeb", color: "#78350f", border: "1px solid #fde68a" },
  "Offer Accepted": { background: "#f0fdf4", color: "#14532d", border: "1px solid #bbf7d0" },
  "Offer Rejected": { background: "#fff7ed", color: "#9a3412", border: "1px solid #fed7aa" },
  Rejected:         { background: "#fef2f2", color: "#991b1b", border: "1px solid #fecaca" },
  Withdrawn:        { background: "#f8fafc", color: "#64748b", border: "1px solid #cbd5e1" },
};

const TIER_BADGE_CLASS = {
  "Super Dream": "tier-super-dream",
  "Dream":       "tier-dream",
  "Normal":      "tier-normal",
  "Unplaced":    "tier-unplaced",
};

// ─── Utility ─────────────────────────────────────────────────────────────────

function formatDate(dateStr) {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

// ─── Components ──────────────────────────────────────────────────────────────

function TierBadge({ tier }) {
  return (
    <span className={`tier-badge ${TIER_BADGE_CLASS[tier] ?? "tier-unplaced"}`}>
      {tier ?? "Unplaced"}
    </span>
  );
}

// ─── Main component ───────────────────────────────────────────────────────────

function Applications() {
  const [students,        setStudents]        = useState([]);
  const [drives,          setDrives]          = useState([]);
  const [allApplications, setAllApplications] = useState([]);
  const [hasSearched,     setHasSearched]     = useState(false);
  const [successMessage,  setSuccessMessage]  = useState(null);
  const [errorMessage,    setErrorMessage]    = useState(null);
  const [savingId,        setSavingId]        = useState(null);

  const [cohort, setCohort] = useState({
    branch: "CSE", program: "B.Tech", batchYear: 2025,
  });
  const [filters, setFilters] = useState({
    studentId: "All", driveId: "All", status: "All",
  });

  useEffect(() => {
    if (!successMessage) return;
    const t = setTimeout(() => setSuccessMessage(null), 4000);
    return () => clearTimeout(t);
  }, [successMessage]);

  useEffect(() => {
    if (!errorMessage) return;
    const t = setTimeout(() => setErrorMessage(null), 5000);
    return () => clearTimeout(t);
  }, [errorMessage]);

  useEffect(() => {
    getStudents()
      .then((res) => setStudents(res.data))
      .catch((err) => console.error("Error loading students:", err));
    getAllDrives()
      .then((res) => setDrives(res.data))
      .catch((err) => console.error("Error loading drives:", err));
  }, []);

  // ── Derived ───────────────────────────────────────────────────────────────

  const cohortStudents = students.filter(
    (s) =>
      s.department?.branch   === cohort.branch &&
      s.department?.program  === cohort.program &&
      s.batchYear            === cohort.batchYear
  );

  const uniqueApplicants = new Set(
    allApplications.map((a) => a.student?.studentId).filter(Boolean)
  ).size;

  const displayedApplications = allApplications
    .filter((a) =>
      filters.studentId === "All" ||
      String(a.student?.studentId) === filters.studentId
    )
    .filter((a) =>
      filters.driveId === "All" ||
      String(a.drive?.driveId) === filters.driveId
    )
    .filter((a) => filters.status === "All" || a.status === filters.status)
    // Normalize: null/empty status defaults to "Applied" so the cell never renders "X"
    .map((a) => ({ ...a, status: a.status || "Applied" }));

  // ── Handlers ─────────────────────────────────────────────────────────────

  const handleSearch = async () => {
    try {
      const res = await getFilteredApplications(
        cohort.branch, cohort.program, cohort.batchYear
      );
      setAllApplications(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error loading applications:", err);
    }
  };

  const handleQuickStatusUpdate = async (id, newStatus) => {
    setSavingId(id);
    try {
      await updateApplicationStatus(id, newStatus);
      setAllApplications((prev) =>
        prev.map((a) => {
          if (a.applicationId !== id) return a;
          // If selected, also update the student's placement tier optimistically
          if (newStatus === "Selected" && a.drive?.company?.tier) {
            return {
              ...a,
              status: newStatus,
              student: { ...a.student, placementTier: a.drive.company.tier },
            };
          }
          return { ...a, status: newStatus };
        })
      );
      setSuccessMessage(`Status updated to "${newStatus}".`);
    } catch (err) {
      const msg = err.response?.data?.message
               || err.response?.data?.error
               || "Status update failed.";
      setErrorMessage(msg);
    } finally {
      setSavingId(null);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this application?")) return;
    try {
      await deleteApplication(id);
      setSuccessMessage("Application deleted.");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
    }
  };

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <Layout>

      {/* Page header */}
      <div className="d-flex align-items-center mb-3">
        <h2 className="mb-0">Applications</h2>
        <span
          className="ms-3 badge bg-secondary-subtle text-secondary-emphasis fw-normal"
          style={{ fontSize: "0.72rem", padding: "4px 10px", borderRadius: 6 }}
        >
          Students apply via the Student Portal
        </span>
      </div>

      {/* Success toast */}
      {successMessage && (
        <div className="alert alert-success d-flex align-items-center py-2 mb-3" role="alert">
          <span className="me-2">✅</span>
          <span className="flex-grow-1">{successMessage}</span>
          <button type="button" className="btn-close btn-close-sm" onClick={() => setSuccessMessage(null)} />
        </div>
      )}

      {/* Error toast */}
      {errorMessage && (
        <div className="alert alert-danger d-flex align-items-center py-2 mb-3" role="alert">
          <span className="me-2">⚠️</span>
          <span className="flex-grow-1">{errorMessage}</span>
          <button type="button" className="btn-close btn-close-sm" onClick={() => setErrorMessage(null)} />
        </div>
      )}

      {/* ── Filter Panel ────────────────────────────────────────────────── */}
      <div className="card mb-3">
        <div className="card-body" style={{ padding: "16px 20px" }}>

          {/* Row 1: Cohort */}
          <div className="row g-2 mb-2">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Branch</label>
              <select
                className="form-select form-select-sm"
                value={cohort.branch}
                onChange={(e) => setCohort({ ...cohort, branch: e.target.value })}
              >
                {DEPT_BY_PROGRAM[cohort.program].map((b) => (
                  <option key={b} value={b}>{b}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Program</label>
              <select
                className="form-select form-select-sm"
                value={cohort.program}
                onChange={(e) => {
                  const prog = e.target.value;
                  setCohort({ ...cohort, program: prog, branch: DEPT_BY_PROGRAM[prog][0] });
                  setFilters({ ...filters, studentId: "All" });
                }}
              >
                {PROGRAMS.map((p) => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Batch Year</label>
              <select
                className="form-select form-select-sm"
                value={cohort.batchYear}
                onChange={(e) => {
                  setCohort({ ...cohort, batchYear: parseInt(e.target.value) });
                  setFilters({ ...filters, studentId: "All" });
                }}
              >
                {BATCH_YEARS.map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
          </div>

          {/* Row 2: Student + Drive */}
          <div className="row g-2 mb-2">
            <div className="col-md-6">
              <label className="form-label form-label-sm mb-1 fw-semibold">
                Student
                <span className="text-muted fw-normal ms-1">
                  ({cohortStudents.length} in cohort)
                </span>
              </label>
              <select
                className="form-select form-select-sm"
                value={filters.studentId}
                onChange={(e) => setFilters({ ...filters, studentId: e.target.value })}
              >
                <option value="All">All</option>
                {cohortStudents.map((s) => (
                  <option key={s.studentId} value={String(s.studentId)}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label form-label-sm mb-1 fw-semibold">Drive</label>
              <select
                className="form-select form-select-sm"
                value={filters.driveId}
                onChange={(e) => setFilters({ ...filters, driveId: e.target.value })}
              >
                <option value="All">All</option>
                {drives.map((d) => (
                  <option key={d.driveId} value={String(d.driveId)}>
                    {d.company?.companyName} – {d.roleOffered}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Row 3: Status + Load */}
          <div className="row g-2 align-items-end">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Status</label>
              <select
                className="form-select form-select-sm"
                value={filters.status}
                onChange={(e) => setFilters({ ...filters, status: e.target.value })}
              >
                <option value="All">All</option>
                {STATUS_OPTIONS.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary btn-sm w-100" onClick={handleSearch}>
                Load Applications
              </button>
            </div>
          </div>

        </div>
      </div>

      {/* ── Cohort Summary Bar ───────────────────────────────────────────── */}
      {hasSearched && (
        <div className="d-flex gap-0 mb-3 border rounded overflow-hidden">
          <SummaryCell
            label="Cohort"
            value={`${cohort.branch} · ${cohort.program} · ${cohort.batchYear}`}
          />
          <SummaryCell label="Students in Cohort" value={cohortStudents.length} border />
          <SummaryCell label="Unique Applicants"  value={uniqueApplicants}       border />
          <SummaryCell label="Total Applications" value={allApplications.length} border />
        </div>
      )}

      {/* ── Applications Table ───────────────────────────────────────────── */}
      {hasSearched && (
        displayedApplications.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <div style={{ fontSize: "2.5rem" }}>🔍</div>
            <p className="fw-semibold mt-2 mb-1 text-dark">No applications found</p>
            <ul className="list-unstyled small">
              <li>Try changing the Branch, Program, or Batch Year</li>
              <li>Select a different Drive or Status filter</li>
              <li>Students apply via the Student Portal — Eligible Drives page</li>
            </ul>
          </div>
        ) : (
          <table className="table table-striped table-bordered table-hover table-sm">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>CGPA</th>
                <th>Placement Status</th>
                <th>Company</th>
                <th>Role</th>
                <th>Date Applied</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayedApplications.map((app) => (
                <tr key={app.applicationId}>
                  <td>{app.applicationId}</td>
                  <td>{app.student?.name ?? "—"}</td>
                  <td>{app.student?.cgpa ?? "—"}</td>
                  <td><TierBadge tier={app.student?.placementTier} /></td>
                  <td>{app.drive?.company?.companyName ?? "—"}</td>
                  <td>{app.drive?.roleOffered ?? "—"}</td>
                  <td>{formatDate(app.applicationDate)}</td>
                  <td>
                    {STATUS_TRANSITIONS[app.status] ? (
                      <>
                        <select
                          className="form-select form-select-sm"
                          value={app.status}
                          disabled={savingId === app.applicationId}
                          onChange={(e) =>
                            handleQuickStatusUpdate(app.applicationId, e.target.value)
                          }
                          style={{
                            minWidth: 185,
                            fontWeight: 600,
                            fontSize: "0.78rem",
                            cursor: "pointer",
                            ...STATUS_SELECT_STYLE[app.status],
                          }}
                        >
                          <option value={app.status}>{app.status}</option>
                          {STATUS_TRANSITIONS[app.status].map((s) => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>
                        {savingId === app.applicationId && (
                          <span className="ms-1 text-muted" style={{ fontSize: "0.72rem" }}>
                            Saving…
                          </span>
                        )}
                      </>
                    ) : (
                      <span
                        style={{
                          display: "inline-block",
                          padding: "5px 14px",
                          borderRadius: 6,
                          fontWeight: 700,
                          fontSize: "0.78rem",
                          ...FINAL_STATUS_STYLE[app.status],
                        }}
                      >
                        {app.status === "Offer Accepted" ? "✓" : app.status === "Offer Released" ? "📩" : app.status === "Withdrawn" ? "–" : "✗"} {app.status}
                      </span>
                    )}
                  </td>
                  <td>
                    <button
                      className="btn btn-sm action-delete"
                      onClick={() => handleDelete(app.applicationId)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      )}
    </Layout>
  );
}

function SummaryCell({ label, value, border }) {
  return (
    <div className={`px-4 py-2 bg-light ${border ? "border-end" : ""}`}>
      <div
        className="text-muted"
        style={{ fontSize: "0.7rem", textTransform: "uppercase", letterSpacing: "0.05em" }}
      >
        {label}
      </div>
      <div className="fw-semibold small">{value}</div>
    </div>
  );
}

export default Applications;
