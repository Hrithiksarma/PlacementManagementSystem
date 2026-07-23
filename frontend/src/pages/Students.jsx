import { useState } from "react";
import Layout from "../components/Layout";
import {
  deleteStudent,
  getFilteredStudents,
  backfillStudentAccounts,
} from "../services/studentService";
import { isAdmin } from "../services/authService";
import "./Students.css";

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t && t !== "Unplaced" ? `Tier ${t}` : (t ?? "—"));

const DEPT_BY_PROGRAM = {
  "B.Tech": ["CSE", "ECE"],
  "M.Tech": ["CSE", "ECE"],
  "PhD":    ["CSE", "ECE", "Science & Mathematics", "HSS"],
};
const BATCH_YEARS = [];
for (let y = 2015; y <= 2030; y++) BATCH_YEARS.push(y);

// Google Drive share URLs look like .../file/d/<ID>/view or ...?id=<ID>.
// Pull the file ID out so we can embed a preview iframe.
function driveFileId(url) {
  if (!url) return null;
  const byPath = url.match(/\/d\/([-\w]{25,})/);
  if (byPath) return byPath[1];
  const byQuery = url.match(/[?&]id=([-\w]{25,})/);
  if (byQuery) return byQuery[1];
  const loose = url.match(/[-\w]{25,}/);
  return loose ? loose[0] : null;
}

function DocBlock({ label, url }) {
  const fileId = driveFileId(url);
  return (
    <div className="mb-3">
      <div className="d-flex align-items-center gap-2 mb-1">
        <span className="fw-semibold">{label}</span>
        {url ? (
          <a href={url} target="_blank" rel="noreferrer" className="btn btn-sm btn-outline-primary py-0">
            Open in new tab ↗
          </a>
        ) : (
          <span className="text-muted" style={{ fontSize: "0.85rem" }}>Not provided</span>
        )}
      </div>
      {url && fileId && (
        <div style={{ height: 360, border: "1px solid #e2e8f0", borderRadius: 8, overflow: "hidden" }}>
          <iframe
            title={`${label} preview`}
            src={`https://drive.google.com/file/d/${fileId}/preview`}
            width="100%"
            height="100%"
            style={{ border: "none" }}
          />
        </div>
      )}
    </div>
  );
}

function StudentDetailsModal({ student, onClose }) {
  if (!student) return null;
  const field = (label, value) => (
    <div className="col-md-6 mb-2">
      <div className="text-muted" style={{ fontSize: "0.75rem" }}>{label}</div>
      <div className="fw-semibold">{value ?? "—"}</div>
    </div>
  );

  return (
    <div
      style={{
        position: "fixed", inset: 0, background: "rgba(15,23,42,0.55)",
        display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050,
        padding: 16,
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: "#fff", borderRadius: 12, padding: "22px 24px",
          width: "min(720px, 96vw)", maxHeight: "92vh", overflowY: "auto",
          boxShadow: "0 20px 50px rgba(0,0,0,0.3)",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h5 className="mb-0">{student.name}</h5>
            <div className="text-muted" style={{ fontFamily: "monospace", fontSize: "0.85rem" }}>
              {student.rollNo ?? "—"}
            </div>
          </div>
          <button className="btn-close" onClick={onClose} aria-label="Close" />
        </div>

        <div className="row g-1">
          {field("Email", student.email)}
          {field("Phone", student.phone)}
          {field("Department", student.department?.deptName)}
          {field("Program / Branch",
            student.department
              ? `${student.department.program ?? "—"} · ${student.department.branch ?? "—"}`
              : "—")}
          {field("Batch Year", student.batchYear)}
          {field("CGPA", student.cgpa)}
          {field("Active Backlogs", student.activeBacklogs)}
          {field("Placement Tier", tierLabelFor(student.placementTier))}
        </div>

        <hr />

        {/* Documents */}
        <div className="mb-2 fw-semibold">Documents</div>
        {!student.resumeUrl && !student.photoUrl && !student.gradeSheetUrl ? (
          <div className="text-muted">No documents on file (manually-added student).</div>
        ) : (
          <>
            <DocBlock label="Resume"     url={student.resumeUrl} />
            <DocBlock label="Photo"      url={student.photoUrl} />
            <DocBlock label="Grade Card" url={student.gradeSheetUrl} />
          </>
        )}
      </div>
    </div>
  );
}

function Students() {
  const [students, setStudents] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [filters, setFilters] = useState({
    department: "CSE",
    program: "B.Tech",
    batchYear: 2025,
  });
  const [backfillLoading, setBackfillLoading] = useState(false);
  const [backfillResult, setBackfillResult]   = useState(null);
  const [backfillError, setBackfillError]     = useState(null);
  const [detailsStudent, setDetailsStudent]   = useState(null);

  const admin = isAdmin();

  const handleSearch = async () => {
    try {
      const res = await getFilteredStudents(
        filters.department,
        filters.program,
        filters.batchYear
      );
      setStudents(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error filtering students:", err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this student?")) return;
    try {
      await deleteStudent(id);
      alert("Student Deleted Successfully");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
      alert("Failed to delete student");
    }
  };

  const handleBackfill = async () => {
    setBackfillLoading(true);
    setBackfillError(null);
    setBackfillResult(null);
    try {
      const res = await backfillStudentAccounts();
      setBackfillResult(res.data);
    } catch (err) {
      setBackfillError(
        err.response?.data?.message ?? "Failed to create missing login accounts."
      );
    } finally {
      setBackfillLoading(false);
    }
  };

  return (
    <Layout>
      {admin && (
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h2 className="mb-0">Students</h2>
          <div className="d-flex gap-2 align-items-center">
            <span
              className="text-muted d-flex align-items-center gap-1"
              style={{ fontSize: "0.82rem" }}
            >
              🎓 Students register through the Google Form (Forms → Students)
            </span>
            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={handleBackfill}
              disabled={backfillLoading}
              title="Create login accounts (username & temp password = roll number) for any student that doesn't have one yet"
            >
              {backfillLoading ? "Creating logins…" : "Create Missing Logins"}
            </button>
          </div>
        </div>
      )}

      {admin && backfillError && (
        <div className="alert alert-danger py-2">{backfillError}</div>
      )}

      {admin && backfillResult && (
        <div className="alert alert-success py-2">
          Created <strong>{backfillResult.created}</strong> new login account(s)
          {backfillResult.skipped > 0 && <> — {backfillResult.skipped} student(s) already had one</>}.
          {backfillResult.created > 0 && (
            <>
              {" "}Username and temporary password = student's roll number
              {backfillResult.createdRollNumbers?.length > 0 && (
                <> ({backfillResult.createdRollNumbers.join(", ")})</>
              )}.
              Students will be prompted to set a new password on first login.
            </>
          )}
        </div>
      )}

      {/* Filter Panel — unchanged */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-3">
              <label className="form-label fw-semibold">Branch</label>
              <select
                className="form-select"
                value={filters.department}
                onChange={(e) => setFilters({ ...filters, department: e.target.value })}
              >
                {DEPT_BY_PROGRAM[filters.program].map((d) => (
                  <option key={d} value={d}>{d}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label fw-semibold">Program</label>
              <select
                className="form-select"
                value={filters.program}
                onChange={(e) => {
                  const prog = e.target.value;
                  setFilters({
                    ...filters,
                    program: prog,
                    department: DEPT_BY_PROGRAM[prog][0],
                  });
                }}
              >
                {PROGRAMS.map((p) => (
                  <option key={p} value={p}>{p}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <label className="form-label fw-semibold">Batch Year</label>
              <select
                className="form-select"
                value={filters.batchYear}
                onChange={(e) => setFilters({ ...filters, batchYear: parseInt(e.target.value) })}
              >
                {BATCH_YEARS.map((y) => (
                  <option key={y} value={y}>{y}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary w-100" onClick={handleSearch}>
                Load Students
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Results Table */}
      {hasSearched && (
        <>
          <p className="text-muted mb-2">
            Showing <strong>{students.length}</strong> student(s) —{" "}
            {filters.program} · {filters.department} · {filters.batchYear} Batch
          </p>
          <table className="table table-striped table-bordered table-hover">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Roll No</th>
                <th>Department</th>
                <th>Batch</th>
                <th>CGPA</th>
                <th>Backlogs</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Tier</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {students.length === 0 ? (
                <tr>
                  <td colSpan="11" className="text-center text-muted py-3">
                    No students found for the selected filters.
                  </td>
                </tr>
              ) : (
                students.map((student) => (
                  <tr key={student.studentId}>
                    <td>{student.studentId}</td>
                    
                    <td>{student.name}</td>
                     <td>
                     <span style={{ fontFamily: "monospace", fontWeight: 400, letterSpacing: "0.04em" }}>
                      {student.rollNo ?? "—"}
                       </span>
                      </td>
                    <td>{student.department?.deptName ?? "—"}</td>
                    <td>{student.batchYear}</td>
                    <td>{student.cgpa}</td>
                    <td>{student.activeBacklogs}</td>
                    <td>{student.phone}</td>
                    <td>{student.email}</td>
                    <td>{tierLabelFor(student.placementTier)}</td>
                    <td>
                      <button
                        className="btn btn-sm btn-outline-secondary me-2"
                        onClick={() => setDetailsStudent(student)}
                      >
                        More Details
                      </button>
                      {admin && (
                        <button
                          className="btn btn-sm action-delete"
                          onClick={() => handleDelete(student.studentId)}
                        >
                          Delete
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </>
      )}

      <StudentDetailsModal
        student={detailsStudent}
        onClose={() => setDetailsStudent(null)}
      />
    </Layout>
  );
}

export default Students;
