import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import {
  getStudentSubmissions,
  syncStudentSubmissions,
  importAllStudents,
  rejectStudentSubmission,
} from "../services/studentFormService";

const TABS = [
  { key: "PENDING",  label: "Submitted (Pending)" },
  { key: "INCLUDED", label: "Imported" },
  { key: "REJECTED", label: "Rejected" },
];

function StudentFormSubmissions() {
  const [tab, setTab]                 = useState("PENDING");
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [syncing, setSyncing]         = useState(false);
  const [importing, setImporting]     = useState(false);
  const [error, setError]             = useState(null);
  const [notice, setNotice]           = useState(null);

  const loadTab = useCallback(async (which) => {
    setLoading(true);
    setError(null);
    try {
      const res = await getStudentSubmissions(which);
      setSubmissions(res.data);
    } catch (err) {
      console.error("Error loading student submissions:", err);
      setError(err.response?.data?.message ?? "Failed to load student submissions.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadTab(tab);
  }, [tab, loadTab]);

  const handleSync = async () => {
    setSyncing(true);
    setError(null);
    setNotice(null);
    try {
      const res = await syncStudentSubmissions();
      setNotice(`Refreshed from Google Form — ${res.data.newSubmissions} new registration(s).`);
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Sync failed. Check the Google Form configuration.");
    } finally {
      setSyncing(false);
    }
  };

  const handleImportAll = async () => {
    if (!window.confirm(`Import all ${submissions.length} pending student(s) into PRMS?`)) return;
    setImporting(true);
    setError(null);
    setNotice(null);
    try {
      const res = await importAllStudents();
      const { created, failed } = res.data;
      setNotice(
        `${created} student(s) imported` +
        (failed > 0 ? ` — ${failed} failed (see reasons below).` : ".")
      );
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Bulk import failed.");
    } finally {
      setImporting(false);
    }
  };

  const handleReject = async (submission) => {
    if (!window.confirm(`Reject ${submission.fullName || submission.email}'s registration?`)) return;
    setError(null);
    setNotice(null);
    try {
      await rejectStudentSubmission(submission.submissionId);
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to reject this submission.");
    }
  };

  return (
    <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="mb-0">Student Registrations</h2>
        <div className="d-flex gap-2 align-items-center">
          <span className="text-muted" style={{ fontSize: "0.82rem" }}>
            🎓 Students register through the Google Form
          </span>
          <button
            className="btn btn-primary btn-sm"
            onClick={handleSync}
            disabled={syncing}
            title="Pull the latest responses from the Google Form's linked sheet"
          >
            {syncing ? "Refreshing…" : "Refresh from Google Form"}
          </button>
        </div>
      </div>

      {notice && <div className="alert alert-success py-2">{notice}</div>}
      {error  && <div className="alert alert-danger py-2">{error}</div>}

      {/* Status tabs */}
      <ul className="nav nav-pills mb-3">
        {TABS.map((t) => (
          <li key={t.key} className="nav-item">
            <button
              className={`nav-link ${tab === t.key ? "active" : ""}`}
              style={{ fontSize: "0.86rem" }}
              onClick={() => setTab(t.key)}
            >
              {t.label}
            </button>
          </li>
        ))}
      </ul>

      {tab === "PENDING" && submissions.length > 0 && (
        <div className="mb-3">
          <button
            className="btn btn-success"
            onClick={handleImportAll}
            disabled={importing}
          >
            {importing ? "Importing…" : `Import All Students (${submissions.length})`}
          </button>
        </div>
      )}

      {loading ? (
        <p className="text-muted">Loading…</p>
      ) : submissions.length === 0 ? (
        <div className="alert alert-warning">
          No {tab.toLowerCase()} registrations. Click "Refresh from Google Form" to pull the latest responses.
        </div>
      ) : (
        <>
          <p className="text-muted mb-2">
            Showing <strong>{submissions.length}</strong> registration(s)
          </p>
          <table className="table table-striped table-bordered table-hover">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Submitted</th>
                <th>Name</th>
                <th>Roll No</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Program</th>
                <th>Branch</th>
                <th>Batch</th>
                <th>CGPA</th>
                <th>Backlogs</th>
                <th>Resume</th>
                <th>Photo</th>
                <th>Grade Card</th>
                <th>{tab === "PENDING" ? "Actions" : "Status"}</th>
              </tr>
            </thead>
            <tbody>
              {submissions.map((s) => (
                <tr key={s.submissionId}>
                  <td>{s.submissionId}</td>
                  <td style={{ fontSize: "0.82rem" }}>{s.submittedAt || "—"}</td>
                  <td className="fw-semibold">
                    {s.fullName || "—"}
                    {s.failureReason && (
                      <div className="text-danger" style={{ fontSize: "0.78rem" }}>
                        ⚠ {s.failureReason}
                      </div>
                    )}
                  </td>
                  <td style={{ fontFamily: "monospace" }}>{s.rollNo || "—"}</td>
                  <td style={{ fontSize: "0.82rem" }}>{s.email || "—"}</td>
                  <td style={{ fontSize: "0.82rem" }}>{s.phone || "—"}</td>
                  <td>{s.program || "—"}</td>
                  <td>{s.branch || "—"}</td>
                  <td>{s.batchYear ?? "—"}</td>
                  <td>{s.cgpa ?? "—"}</td>
                  <td>{s.activeBacklogs ?? "—"}</td>
                  <td>
                    {s.resumeUrl
                      ? <a href={s.resumeUrl} target="_blank" rel="noreferrer">View</a>
                      : "—"}
                  </td>
                  <td>
                    {s.photoUrl
                      ? <a href={s.photoUrl} target="_blank" rel="noreferrer">View</a>
                      : "—"}
                  </td>
                  <td>
                    {s.gradeSheetUrl
                      ? <a href={s.gradeSheetUrl} target="_blank" rel="noreferrer">View</a>
                      : "—"}
                  </td>
                  <td>
                    {s.status === "PENDING" ? (
                      <button
                        className="btn btn-sm btn-outline-danger"
                        onClick={() => handleReject(s)}
                      >
                        Reject
                      </button>
                    ) : s.status === "INCLUDED" ? (
                      <Link to="/admin/students" className="badge bg-success text-decoration-none">
                        ✓ Student #{s.studentId}
                      </Link>
                    ) : (
                      <span className="badge bg-secondary">Rejected</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </>
  );
}

export default StudentFormSubmissions;
