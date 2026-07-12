import { useState } from "react";
import Layout from "../components/Layout";
import {
  deleteStudent,
  getFilteredStudents,
  previewStudent,
  importStudent,
  backfillStudentAccounts,
} from "../services/studentService";
import { isAdmin } from "../services/authService";
import "./Students.css";

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

const DEPT_BY_PROGRAM = {
  "B.Tech": ["CSE", "ECE"],
  "M.Tech": ["CSE", "ECE"],
  "PhD":    ["CSE", "ECE", "Science & Mathematics", "HSS"],
};
const BATCH_YEARS = [];
for (let y = 2015; y <= 2030; y++) BATCH_YEARS.push(y);

function Students() {
  const [students, setStudents] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [filters, setFilters] = useState({
    department: "CSE",
    program: "B.Tech",
    batchYear: 2025,
  });
  const [showImport, setShowImport]       = useState(false);
  const [importRollNo, setImportRollNo]   = useState("");
  const [importPreview, setImportPreview] = useState(null);
  const [importLoading, setImportLoading] = useState(false);
  const [importError, setImportError]     = useState(null);

  const [backfillLoading, setBackfillLoading] = useState(false);
  const [backfillResult, setBackfillResult]   = useState(null);
  const [backfillError, setBackfillError]     = useState(null);

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

  const handlePreview = async () => {
    if (!importRollNo.trim()) return;
    setImportLoading(true);
    setImportError(null);
    setImportPreview(null);
    try {
      const res = await previewStudent(importRollNo.trim());
      setImportPreview(res.data);
    } catch (err) {
      setImportError(
        err.response?.data?.message ?? "Roll number not found in Academic ERP."
      );
    } finally {
      setImportLoading(false);
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

  const handleImport = async () => {
    if (!importPreview) return;
    setImportLoading(true);
    setImportError(null);
    try {
      await importStudent(importRollNo.trim());
      alert(`Student ${importPreview.firstName} ${importPreview.lastName} imported successfully.`);
      setShowImport(false);
      setImportRollNo("");
      setImportPreview(null);
      if (hasSearched) handleSearch();
    } catch (err) {
      setImportError(
        err.response?.data?.message ?? "Import failed. Student may already exist in PRMS."
      );
    } finally {
      setImportLoading(false);
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
              🎓 New students are registered in Academic ERP
            </span>
            <a
              href="http://localhost:5174"
              target="_blank"
              rel="noreferrer"
              className="btn btn-outline-secondary btn-sm"
            >
              Open Academic ERP ↗
            </a>
            <button
              className="btn btn-outline-secondary btn-sm"
              onClick={handleBackfill}
              disabled={backfillLoading}
              title="Create login accounts (username & temp password = roll number) for any student that doesn't have one yet"
            >
              {backfillLoading ? "Creating logins…" : "Create Missing Logins"}
            </button>
            <button
              className="btn btn-primary btn-sm"
              onClick={() => {
                setShowImport(!showImport);
                setImportRollNo("");
                setImportPreview(null);
                setImportError(null);
              }}
            >
              {showImport ? "Cancel Import" : "Import Student"}
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

      {admin && showImport && (
        <div className="card mb-4 border-primary">
          <div className="card-header bg-primary text-white fw-semibold">
            Import Student from Academic ERP
          </div>
          <div className="card-body">

            {/* Roll number input */}
            <div className="d-flex gap-2 align-items-end mb-3">
              <div style={{ flex: 1 }}>
                <label className="form-label fw-semibold">Roll Number</label>
                <input
                  className="form-control"
                  placeholder="e.g. 2511104"
                  value={importRollNo}
                  onChange={(e) => {
                    setImportRollNo(e.target.value);
                    setImportPreview(null);
                    setImportError(null);
                  }}
                  onKeyDown={(e) => e.key === "Enter" && handlePreview()}
                  maxLength={7}
                />
              </div>
              <button
                className="btn btn-outline-primary"
                onClick={handlePreview}
                disabled={importLoading || !importRollNo.trim()}
              >
                {importLoading && !importPreview ? "Searching…" : "Look Up"}
              </button>
            </div>

            {/* Error */}
            {importError && (
              <div className="alert alert-danger py-2">{importError}</div>
            )}

            {/* Preview card */}
            {importPreview && (
              <div className="card bg-light mb-3">
                <div className="card-body">
                  <h6 className="card-title mb-3">
                    {importPreview.firstName}{" "}
                    {importPreview.middleName ? importPreview.middleName + " " : ""}
                    {importPreview.lastName}
                  </h6>
                  <div className="row g-2" style={{ fontSize: "0.88rem" }}>
                    <div className="col-md-4">
                      <span className="text-muted">Roll No</span>
                      <div className="fw-semibold" style={{ fontFamily: "monospace" }}>
                        {importPreview.rollNo}
                      </div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Email</span>
                      <div>{importPreview.email}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Phone</span>
                      <div>{importPreview.phone}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Department</span>
                      <div>{importPreview.department?.deptName ?? "—"}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Program</span>
                      <div>{importPreview.department?.program} · {importPreview.department?.branch}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Admission Year</span>
                      <div>{importPreview.admissionYear}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">CGPA</span>
                      <div>{importPreview.cgpa}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Backlogs</span>
                      <div>{importPreview.activeBacklogs}</div>
                    </div>
                    <div className="col-md-4">
                      <span className="text-muted">Placement tier</span>
                      <div className="text-muted fst-italic">Unplaced (set after import)</div>
                    </div>
                  </div>
                  <button
                    className="btn btn-success mt-3"
                    onClick={handleImport}
                    disabled={importLoading}
                  >
                    {importLoading ? "Importing…" : "Confirm Import"}
                  </button>
                </div>
              </div>
            )}
          </div>
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
                    <td>{student.placementTier}</td>
                    <td>
                      {admin ? (
                        <button
                          className="btn btn-sm action-delete"
                          onClick={() => handleDelete(student.studentId)}
                        >
                          Delete
                        </button>
                      ) : (
                        <span className="text-muted" style={{ fontSize: "0.78rem" }}>
                          View Only
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </>
      )}
    </Layout>
  );
}

export default Students;
