import { useState } from "react";
import { GraduationCap, ExternalLink } from "lucide-react";
import Layout from "../components/Layout";
import {
  deleteStudent,
  getFilteredStudents,
  resetStudentPassword,
  updateStudent,
} from "../services/studentService";
import { isAdmin } from "../services/authService";
import "./Students.css";

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t && t !== "Unplaced" ? `Tier ${t}` : (t ?? "—"));
const TIER_OPTIONS = ["Unplaced", "A", "B", "C"];

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
          <a href={url} target="_blank" rel="noreferrer" className="btn btn-sm btn-outline-primary py-0 d-inline-flex align-items-center gap-1">
            Open in new tab
            <ExternalLink size={11} />
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

function StudentDetailsModal({ student, onClose, onSave }) {
  const [isEditing, setIsEditing] = useState(false);
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [resettingPassword, setResettingPassword] = useState(false);

  if (!student) return null;

  const handleResetPassword = async () => {
    if (!window.confirm(
      `Generate a new temporary password and email it to ${student.name} (${student.email})?`
    )) return;
    setResettingPassword(true);
    try {
      await resetStudentPassword(student.studentId);
      alert("New password generated and emailed to the student.");
    } catch (err) {
      console.error(err);
      alert(err.response?.data?.message ?? "Failed to reset password.");
    } finally {
      setResettingPassword(false);
    }
  };

  const field = (label, value) => (
    <div className="col-md-6 mb-2">
      <div className="text-muted" style={{ fontSize: "0.75rem" }}>{label}</div>
      <div className="fw-semibold">{value ?? "—"}</div>
    </div>
  );

  const editField = (key) => ({
    value: form[key] ?? "",
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  });

  const startEdit = () => {
    setForm({
      name: student.name ?? "",
      email: student.email ?? "",
      phone: student.phone ?? "",
      cgpa: student.cgpa ?? "",
      activeBacklogs: student.activeBacklogs ?? "",
      placementTier: student.placementTier ?? "Unplaced",
      optedForHigherStudies: student.optedForHigherStudies ?? false,
      resumeUrl: student.resumeUrl ?? "",
      photoUrl: student.photoUrl ?? "",
      gradeSheetUrl: student.gradeSheetUrl ?? "",
    });
    setError(null);
    setIsEditing(true);
  };

  const cancelEdit = () => {
    setIsEditing(false);
    setForm(null);
    setError(null);
  };

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    try {
      await onSave(student.studentId, {
        ...student,
        name: form.name,
        email: form.email,
        phone: form.phone,
        cgpa: form.cgpa === "" ? null : parseFloat(form.cgpa),
        activeBacklogs: form.activeBacklogs === "" ? null : parseInt(form.activeBacklogs, 10),
        placementTier: form.placementTier,
        optedForHigherStudies: form.optedForHigherStudies,
        resumeUrl: form.resumeUrl || null,
        photoUrl: form.photoUrl || null,
        gradeSheetUrl: form.gradeSheetUrl || null,
      });
      setIsEditing(false);
      setForm(null);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message ?? "Failed to save changes.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div
      style={{
        position: "fixed", inset: 0, background: "rgba(15,23,42,0.55)",
        display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050,
        padding: 16,
      }}
      onClick={isEditing ? undefined : onClose}
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
          <div className="d-flex align-items-center gap-2">
            {!isEditing && (
              <>
                <button
                  className="btn btn-sm btn-outline-primary"
                  onClick={handleResetPassword}
                  disabled={resettingPassword}
                >
                  {resettingPassword ? "Sending…" : "Reset Password"}
                </button>
                <button className="btn btn-sm action-edit" onClick={startEdit}>
                  Edit
                </button>
              </>
            )}
            <button className="btn-close" onClick={onClose} aria-label="Close" />
          </div>
        </div>

        {error && <div className="alert alert-danger py-2">{error}</div>}

        {!isEditing ? (
          <>
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
              {field("Higher Studies", student.optedForHigherStudies ? "Yes" : "No")}
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
          </>
        ) : (
          <>
            <div className="row g-2">
              <div className="col-md-6">
                <label className="form-label">Name</label>
                <input className="form-control" {...editField("name")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input className="form-control" type="email" {...editField("email")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Phone</label>
                <input className="form-control" {...editField("phone")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Placement Tier</label>
                <select className="form-select" {...editField("placementTier")}>
                  {TIER_OPTIONS.map((t) => (
                    <option key={t} value={t}>{tierLabelFor(t)}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6 d-flex align-items-end">
                <div className="form-check">
                  <input
                    className="form-check-input"
                    type="checkbox"
                    id="optedForHigherStudies"
                    checked={form.optedForHigherStudies}
                    onChange={(e) => setForm({ ...form, optedForHigherStudies: e.target.checked })}
                  />
                  <label className="form-check-label" htmlFor="optedForHigherStudies">
                    Opted for Higher Studies
                  </label>
                </div>
              </div>
              <div className="col-md-6">
                <label className="form-label">CGPA</label>
                <input className="form-control" type="number" step="0.01" min="0" max="10" {...editField("cgpa")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Active Backlogs</label>
                <input className="form-control" type="number" step="1" min="0" {...editField("activeBacklogs")} />
              </div>
            </div>

            <hr />

            <div className="mb-2 fw-semibold">Documents</div>
            <div className="mb-3">
              <label className="form-label">Resume URL</label>
              <input className="form-control" placeholder="https://drive.google.com/..." {...editField("resumeUrl")} />
              <DocBlock label="Resume" url={form.resumeUrl} />
            </div>
            <div className="mb-3">
              <label className="form-label">Photo URL</label>
              <input className="form-control" placeholder="https://drive.google.com/..." {...editField("photoUrl")} />
              <DocBlock label="Photo" url={form.photoUrl} />
            </div>
            <div className="mb-3">
              <label className="form-label">Grade Card URL</label>
              <input className="form-control" placeholder="https://drive.google.com/..." {...editField("gradeSheetUrl")} />
              <DocBlock label="Grade Card" url={form.gradeSheetUrl} />
            </div>

            <div className="d-flex gap-2 justify-content-end">
              <button className="btn btn-outline-secondary" onClick={cancelEdit} disabled={saving}>
                Cancel
              </button>
              <button className="btn btn-success" onClick={handleSave} disabled={saving}>
                {saving ? "Saving…" : "Save"}
              </button>
            </div>
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
      alert(err.response?.data?.message ?? "Failed to delete student");
    }
  };

  const handleSaveStudent = async (id, payload) => {
    const res = await updateStudent(id, payload);
    const saved = res.data;
    setStudents((prev) => prev.map((s) => (s.studentId === id ? saved : s)));
    setDetailsStudent(saved);
  };

  return (
    <Layout>
      {admin && (
        <div className="d-flex justify-content-between align-items-center mb-3">
          <h2 className="mb-0">Students</h2>
          <span
            className="text-muted d-flex align-items-center gap-1"
            style={{ fontSize: "0.82rem" }}
          >
            <GraduationCap size={13} />
            Students register through the Google Form (Forms → Students)
          </span>
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
        onSave={handleSaveStudent}
      />
    </Layout>
  );
}

export default Students;
