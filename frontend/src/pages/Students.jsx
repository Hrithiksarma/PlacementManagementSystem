import { useEffect, useState } from "react";
import Layout from "../components/Layout";
import {
  addStudent,
  updateStudent,
  deleteStudent,
  getFilteredStudents,
} from "../services/studentService";
import { getDepartments } from "../services/departmentService";
import { isAdmin } from "../services/authService";
import "./Students.css";

const PROGRAMS = ["B.Tech", "M.Tech", "PhD"];

const DEPT_BY_PROGRAM = {
  "B.Tech": ["CSE", "ECE"],
  "M.Tech": ["CSE", "ECE"],
  "PhD":    ["CSE", "ECE", "Science & Mathematics", "HSS"],
};
const PLACEMENT_TIERS = ["Normal", "Dream", "Super Dream"];

const BATCH_YEARS = [];
for (let y = 2015; y <= 2030; y++) BATCH_YEARS.push(y);

const emptyForm = {
  name: "",
  deptId: "",
  batchYear: 2025,
  cgpa: "",
  activeBacklogs: "",
  phone: "",
  email: "",
  placementTier: "Normal",
};

function Students() {
  const [departments, setDepartments] = useState([]);
  const [students, setStudents] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [filters, setFilters] = useState({
    department: "CSE",
    program: "B.Tech",
    batchYear: 2025,
  });

  const admin = isAdmin();

  useEffect(() => {
    getDepartments()
      .then((res) => setDepartments(res.data))
      .catch((err) => console.error("Error loading departments:", err));
  }, []);

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

  const buildPayload = () => ({
    name: form.name,
    department: form.deptId ? { deptId: parseInt(form.deptId) } : null,
    batchYear: parseInt(form.batchYear),
    cgpa: parseFloat(form.cgpa),
    activeBacklogs: parseInt(form.activeBacklogs),
    phone: form.phone,
    email: form.email,
    placementTier: form.placementTier,
  });

  const handleSaveStudent = async () => {
    try {
      if (editingId) {
        await updateStudent(editingId, buildPayload());
        alert("Student Updated Successfully");
      } else {
        await addStudent(buildPayload());
        alert("Student Added Successfully");
      }
      if (hasSearched) handleSearch();
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
    } catch (err) {
      console.error(err);
    }
  };

  const handleEdit = (student) => {
    setForm({
      name: student.name,
      deptId: student.department?.deptId ?? "",
      batchYear: student.batchYear,
      cgpa: student.cgpa,
      activeBacklogs: student.activeBacklogs,
      phone: student.phone,
      email: student.email,
      placementTier: student.placementTier ?? "Normal",
    });
    setEditingId(student.studentId);
    setShowForm(true);
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

  const field = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  });

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <div className="d-flex align-items-center gap-2">
          <h2 className="mb-0">Students</h2>
          {!admin && (
            <span className="badge bg-info text-white" style={{ fontSize: "0.7rem" }}>
              View Only
            </span>
          )}
        </div>
        {admin && (
          <button
            className="btn btn-primary"
            onClick={() => {
              setShowForm(!showForm);
              if (showForm) { setEditingId(null); setForm(emptyForm); }
            }}
          >
            {showForm ? "Cancel" : "Add Student"}
          </button>
        )}
      </div>

      {/* Filter Panel */}
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

      {/* Add / Edit Form — Admin only */}
      {admin && showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <div className="row g-2">
              <div className="col-md-6">
                <label className="form-label">Name</label>
                <input className="form-control" placeholder="Full name" {...field("name")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Department</label>
                <select className="form-select" {...field("deptId")}>
                  <option value="">Select department</option>
                  {departments.map((d) => (
                    <option key={d.deptId} value={d.deptId}>
                      {d.deptName} – {d.program}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Batch Year</label>
                <select className="form-select" {...field("batchYear")}>
                  {BATCH_YEARS.map((y) => (
                    <option key={y} value={y}>{y}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Placement Tier</label>
                <select className="form-select" {...field("placementTier")}>
                  {PLACEMENT_TIERS.map((t) => (
                    <option key={t} value={t}>{t}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">CGPA</label>
                <input className="form-control" placeholder="0.00 – 10.00" type="number" step="0.01" min="0" max="10" {...field("cgpa")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Active Backlogs</label>
                <input className="form-control" placeholder="0" type="number" min="0" {...field("activeBacklogs")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Phone</label>
                <input className="form-control" placeholder="10-digit number" {...field("phone")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Email</label>
                <input className="form-control" placeholder="student@email.com" type="email" {...field("email")} />
              </div>
            </div>
            <button className="btn btn-success mt-3" onClick={handleSaveStudent}>
              {editingId ? "Update Student" : "Save Student"}
            </button>
          </div>
        </div>
      )}

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
                  <td colSpan="10" className="text-center text-muted py-3">
                    No students found for the selected filters.
                  </td>
                </tr>
              ) : (
                students.map((student) => (
                  <tr key={student.studentId}>
                    <td>{student.studentId}</td>
                    <td>{student.name}</td>
                    <td>{student.department?.deptName ?? "—"}</td>
                    <td>{student.batchYear}</td>
                    <td>{student.cgpa}</td>
                    <td>{student.activeBacklogs}</td>
                    <td>{student.phone}</td>
                    <td>{student.email}</td>
                    <td>{student.placementTier}</td>
                    <td>
                      {admin ? (
                        <>
                          <button
                            className="btn btn-sm action-edit me-2"
                            onClick={() => handleEdit(student)}
                          >
                            Edit
                          </button>
                          <button
                            className="btn btn-sm action-delete"
                            onClick={() => handleDelete(student.studentId)}
                          >
                            Delete
                          </button>
                        </>
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
