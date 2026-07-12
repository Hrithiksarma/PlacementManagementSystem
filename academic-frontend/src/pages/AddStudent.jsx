import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../components/Layout.jsx";
import { createStudent } from "../services/api.js";

const DEPARTMENTS = [
  { deptId: 1, label: "B.Tech — CSE  (Section C13)", program: "B.Tech" },
  { deptId: 2, label: "B.Tech — CSE  (Section C14)", program: "B.Tech" },
  { deptId: 3, label: "B.Tech — ECE  (Section E13)", program: "B.Tech" },
  { deptId: 4, label: "B.Tech — ECE  (Section E14)", program: "B.Tech" },
  { deptId: 5, label: "M.Tech — CSE  (Section M13)", program: "M.Tech" },
  { deptId: 6, label: "M.Tech — CSE  (Section M14)", program: "M.Tech" },
  { deptId: 7, label: "M.Tech — ECE  (Section ME13)", program: "M.Tech" },
  { deptId: 8, label: "M.Tech — ECE  (Section ME14)", program: "M.Tech" },
];

const EMPTY = {
  firstName: "", middleName: "", lastName: "",
  gender: "", email: "", phone: "", address: "",
  deptId: "", admissionYear: new Date().getFullYear(),
  cgpa: "", activeBacklogs: 0,
  class10Percentage: "", class12Percentage: "",
  graduationCgpa: "",
};

export default function AddStudent() {
  const navigate = useNavigate();
  const [form, setForm]              = useState(EMPTY);
  const [loading, setLoading]        = useState(false);
  const [error, setError]            = useState(null);
  const [createdStudent, setCreated] = useState(null);

  const selectedDept = DEPARTMENTS.find(d => d.deptId === parseInt(form.deptId));
  const isMTech      = selectedDept?.program === "M.Tech";

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(f => ({ ...f, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setLoading(true);

    const payload = {
      firstName:         form.firstName.trim(),
      middleName:        form.middleName.trim() || null,
      lastName:          form.lastName.trim(),
      gender:            form.gender || null,
      email:             form.email.trim(),
      phone:             form.phone.trim(),
      address:           form.address.trim() || null,
      deptId:            parseInt(form.deptId),
      admissionYear:     parseInt(form.admissionYear),
      cgpa:              parseFloat(form.cgpa),
      activeBacklogs:    parseInt(form.activeBacklogs) || 0,
      class10Percentage: form.class10Percentage ? parseFloat(form.class10Percentage) : null,
      class12Percentage: form.class12Percentage ? parseFloat(form.class12Percentage) : null,
      graduationCgpa:    isMTech && form.graduationCgpa ? parseFloat(form.graduationCgpa) : null,
    };

    try {
      const res = await createStudent(payload);
      setCreated(res.data);
    } catch (err) {
      setError(
        err.response?.data?.message ??
        "Failed to register student. Check all required fields."
      );
    } finally {
      setLoading(false);
    }
  };

  // ── Success screen ───────────────────────────────────────────
  if (createdStudent) {
    return (
      <Layout>
        <div className="card" style={{ maxWidth: 520, margin: "3rem auto" }}>
          <div className="success-card">
            <div className="success-icon">✓</div>
            <h2 style={{ fontSize: "1.3rem", marginBottom: ".5rem" }}>
              Student Registered Successfully
            </h2>
            <p className="text-muted">
              {createdStudent.firstName}{" "}
              {createdStudent.middleName ? createdStudent.middleName + " " : ""}
              {createdStudent.lastName} — {createdStudent.department?.program}{" "}
              {createdStudent.department?.branch}
            </p>
            <div className="roll-display">
              <label>Roll Number Generated</label>
              <div className="roll-number">{createdStudent.rollNo}</div>
            </div>
            <p className="roll-note">
              📋 Share this roll number with the <strong>Placement Cell</strong>.<br />
              They will use it to import this student into PRMS.
            </p>
            <div style={{ display:"flex", gap:"1rem", justifyContent:"center", marginTop:"1.5rem" }}>
              <button className="btn btn-primary"
                onClick={() => { setCreated(null); setForm(EMPTY); }}>
                Add Another Student
              </button>
              <button className="btn btn-secondary"
                onClick={() => navigate("/")}>
                Back to Dashboard
              </button>
            </div>
          </div>
        </div>
      </Layout>
    );
  }

  // ── Form ─────────────────────────────────────────────────────
  return (
    <Layout>
      <div style={{ maxWidth: 760, margin: "0 auto" }}>
        <div className="page-header">
          <span className="page-title">Register New Student</span>
          <button className="btn btn-secondary" onClick={() => navigate("/")}>← Back</button>
        </div>

        <div className="card">
          {error && (
            <div style={{
              background:"#fef2f2", border:"1px solid #fecaca",
              color:"#dc2626", padding:"10px 14px",
              borderRadius:"8px", marginBottom:"1.2rem", fontSize:".88rem"
            }}>{error}</div>
          )}

          <form onSubmit={handleSubmit}>

            {/* ── Personal Details ── */}
            <p style={{ fontWeight:600, marginBottom:".8rem", color:"var(--primary-dark)" }}>
              Personal Details
            </p>
            <div className="form-row-3">
              <div className="form-group">
                <label>First Name *</label>
                <input name="firstName" value={form.firstName}
                  onChange={handleChange} placeholder="Aarav" required />
              </div>
              <div className="form-group">
                <label>Middle Name</label>
                <input name="middleName" value={form.middleName}
                  onChange={handleChange} placeholder="(optional)" />
              </div>
              <div className="form-group">
                <label>Last Name *</label>
                <input name="lastName" value={form.lastName}
                  onChange={handleChange} placeholder="Sharma" required />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label>Gender</label>
                <select name="gender" value={form.gender} onChange={handleChange}>
                  <option value="">Select gender</option>
                  <option>Male</option>
                  <option>Female</option>
                  <option>Other</option>
                </select>
              </div>
              <div className="form-group">
                <label>Phone *</label>
                <input name="phone" value={form.phone}
                  onChange={handleChange} placeholder="9876501234" required />
              </div>
            </div>

            <div className="form-group">
              <label>Email *</label>
              <input type="email" name="email" value={form.email}
                onChange={handleChange}
                placeholder="aarav.sharma@iiitg.ac.in" required />
            </div>

            <div className="form-group">
              <label>Address</label>
              <textarea name="address" value={form.address}
                onChange={handleChange}
                placeholder="House No, Street, City, State, PIN"
                rows="2"
                style={{
                  width:"100%", padding:"9px 12px",
                  border:"1.5px solid var(--border)", borderRadius:"8px",
                  fontSize:".92rem", resize:"vertical", fontFamily:"inherit"
                }}
              />
            </div>

            {/* ── Academic Placement ── */}
            <p style={{ fontWeight:600, margin:"1.2rem 0 .8rem", color:"var(--primary-dark)" }}>
              Department & Program
            </p>

            <div className="form-group">
              <label>Department *</label>
              <select name="deptId" value={form.deptId}
                onChange={handleChange} required>
                <option value="">Select department</option>
                {DEPARTMENTS.map(d => (
                  <option key={d.deptId} value={d.deptId}>{d.label}</option>
                ))}
              </select>
            </div>

            <div className="form-row-3">
              <div className="form-group">
                <label>Admission Year *</label>
                <input type="number" name="admissionYear"
                  value={form.admissionYear} onChange={handleChange}
                  min="2000" max="2099" required />
              </div>
              <div className="form-group">
                <label>Current CGPA *</label>
                <input type="number" name="cgpa" value={form.cgpa}
                  onChange={handleChange} step="0.01" min="0" max="10"
                  placeholder="8.50" required />
              </div>
              <div className="form-group">
                <label>Active Backlogs</label>
                <input type="number" name="activeBacklogs"
                  value={form.activeBacklogs} onChange={handleChange}
                  min="0" />
              </div>
            </div>

            {/* ── Academic History ── */}
            <p style={{ fontWeight:600, margin:"1.2rem 0 .8rem", color:"var(--primary-dark)" }}>
              Academic History
            </p>

            <div className="form-row">
              <div className="form-group">
                <label>Class 10 Percentage</label>
                <input type="number" name="class10Percentage"
                  value={form.class10Percentage} onChange={handleChange}
                  step="0.01" min="0" max="100" placeholder="92.40" />
              </div>
              <div className="form-group">
                <label>Class 12 Percentage</label>
                <input type="number" name="class12Percentage"
                  value={form.class12Percentage} onChange={handleChange}
                  step="0.01" min="0" max="100" placeholder="88.60" />
              </div>
            </div>

            {/* Graduation CGPA — only visible for M.Tech departments */}
            {isMTech && (
              <div className="form-group"
                style={{
                  background:"var(--primary-light)",
                  border:"1px solid var(--border)",
                  borderRadius:"8px", padding:"12px 14px"
                }}>
                <label style={{ color:"var(--primary-dark)" }}>
                  B.Tech CGPA at Admission *
                  <span style={{ fontWeight:400, fontSize:".8rem", marginLeft:6 }}>
                    (Required for M.Tech applicants)
                  </span>
                </label>
                <input type="number" name="graduationCgpa"
                  value={form.graduationCgpa} onChange={handleChange}
                  step="0.01" min="0" max="10" placeholder="7.80"
                  required={isMTech}
                  style={{ marginTop:6 }}
                />
              </div>
            )}

            <div style={{ display:"flex", gap:"1rem", marginTop:"1.2rem" }}>
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? "Registering…" : "Register Student & Generate Roll No"}
              </button>
              <button type="button" className="btn btn-secondary"
                onClick={() => setForm(EMPTY)}>
                Clear
              </button>
            </div>

          </form>
        </div>
      </div>
    </Layout>
  );
}
