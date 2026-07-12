import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Layout from "../components/Layout.jsx";
import { getStudents } from "../services/api.js";

export default function Dashboard() {
  const navigate = useNavigate();
  const [students, setStudents] = useState([]);
  const [loading, setLoading]   = useState(true);
  const [search,  setSearch]    = useState("");

  useEffect(() => {
    getStudents()
      .then((res) => setStudents(res.data))
      .catch(() => setStudents([]))
      .finally(() => setLoading(false));
  }, []);

  const filtered = students.filter((s) => {
    const q = search.toLowerCase();
    const fullName = `${s.firstName} ${s.middleName ?? ""} ${s.lastName}`.toLowerCase();
    return (
      fullName.includes(q) ||
      s.rollNo.includes(q) ||
      s.email.toLowerCase().includes(q)
    );
  });

  const btechCount = students.filter((s) => s.department?.program === "B.Tech").length;
  const mtechCount = students.filter((s) => s.department?.program === "M.Tech").length;

  return (
    <Layout>
      {/* Stats */}
      <div className="stat-row">
        <div className="stat-card">
          <div className="stat-num">{students.length}</div>
          <div className="stat-label">Total Students</div>
        </div>
        <div className="stat-card">
          <div className="stat-num">{btechCount}</div>
          <div className="stat-label">B.Tech Students</div>
        </div>
        <div className="stat-card">
          <div className="stat-num">{mtechCount}</div>
          <div className="stat-label">M.Tech Students</div>
        </div>
      </div>

      {/* Header row */}
      <div className="page-header">
        <span className="page-title">All Students</span>
        <button
          className="btn btn-primary"
          onClick={() => navigate("/add-student")}
        >
          + Add Student
        </button>
      </div>

      {/* Search */}
      <div className="card" style={{ padding: "1rem" }}>
        <input
          style={{
            width: "100%", padding: "9px 12px",
            border: "1.5px solid var(--border)", borderRadius: "8px",
            fontSize: ".92rem", outline: "none",
          }}
          placeholder="Search by name, roll number or email…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      {/* Table */}
      <div className="card" style={{ padding: 0, overflowX: "auto" }}>
        {loading ? (
          <p className="empty-row">Loading students…</p>
        ) : (
          <table style={{ minWidth: 1600 }}>
            <thead>
              <tr>
                <th style={{ minWidth: 100 }}>Roll No</th>
                <th style={{ minWidth: 150 }}>Name</th>
                <th style={{ minWidth: 80  }}>Dept</th>
                <th style={{ minWidth: 80  }}>Program</th>
                <th style={{ minWidth: 60  }}>Year</th>
                <th style={{ minWidth: 70  }}>CGPA</th>
                <th style={{ minWidth: 75  }}>Backlogs</th>
                <th style={{ minWidth: 75  }}>Gender</th>
                <th style={{ minWidth: 200 }}>Email</th>
                <th style={{ minWidth: 120 }}>Phone</th>
                <th style={{ minWidth: 260 }}>Address</th>
                <th style={{ minWidth: 90  }}>Class 10%</th>
                <th style={{ minWidth: 90  }}>Class 12%</th>
                <th style={{ minWidth: 100 }}>Grad. CGPA</th>
                <th style={{ minWidth: 80  }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan="15" className="empty-row">
                    No students found.
                  </td>
                </tr>
              ) : (
                filtered.map((s) => (
                  <tr key={s.rollNo}>
                    <td><span className="roll-badge">{s.rollNo}</span></td>
                    <td>
                      {s.firstName}{" "}
                      {s.middleName ? s.middleName + " " : ""}
                      {s.lastName}
                    </td>
                    <td>{s.department?.branch ?? "—"}</td>
                    <td>{s.department?.program ?? "—"}</td>
                    <td>{s.admissionYear}</td>
                    <td>{s.cgpa}</td>
                    <td>{s.activeBacklogs}</td>
                    <td>{s.gender ?? "—"}</td>
                    <td>{s.email}</td>
                    <td>{s.phone}</td>
                    <td style={{ whiteSpace: "normal", lineHeight: 1.4 }}>{s.address ?? "—"}</td>
                    <td>{s.class10Percentage ?? "—"}</td>
                    <td>{s.class12Percentage ?? "—"}</td>
                    <td>{s.graduationCgpa ?? "—"}</td>
                    <td>
                      <button
                        className="btn btn-secondary"
                        style={{ padding: "4px 10px", fontSize: ".82rem" }}
                        onClick={() => navigate(`/edit-student/${s.rollNo}`)}
                      >
                        Edit
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </Layout>
  );
}
