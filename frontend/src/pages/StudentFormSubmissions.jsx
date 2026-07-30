import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import {
  getStudentSubmissions,
  syncStudentSubmissions,
  importAllStudents,
  importSelectedStudents,
  includeStudentSubmission,
  rejectStudentSubmission,
  flagStudentSubmission,
} from "../services/studentFormService";

const TABS = [
  { key: "PENDING",  label: "Submitted (Pending)" },
  { key: "INCLUDED", label: "Imported" },
  { key: "REJECTED", label: "Rejected" },
];

function StudentFormSubmissions({ highlightSubmissionId, onHighlightHandled }) {
  const [tab, setTab]                 = useState("PENDING");
  const [submissions, setSubmissions] = useState([]);
  const [loading, setLoading]         = useState(false);
  const [syncing, setSyncing]         = useState(false);
  const [importing, setImporting]     = useState(false);
  const [error, setError]             = useState(null);
  const [notice, setNotice]           = useState(null);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [flagTarget, setFlagTarget]   = useState(null);
  const [flagComment, setFlagComment] = useState("");
  const [flagging, setFlagging]       = useState(false);

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
    setSelectedIds(new Set());
  }, [tab, loadTab]);

  useEffect(() => {
    if (highlightSubmissionId) setTab("PENDING");
  }, [highlightSubmissionId]);

  useEffect(() => {
    if (!highlightSubmissionId || loading) return;
    const el = document.getElementById(`submission-${highlightSubmissionId}`);
    if (!el) return;
    el.scrollIntoView({ behavior: "smooth", block: "center" });
    const previousBackground = el.style.backgroundColor;
    el.style.backgroundColor = "#fff3cd";
    el.style.transition = "background-color 1.5s ease";
    setTimeout(() => { el.style.backgroundColor = previousBackground; }, 2000);
    onHighlightHandled?.();
  }, [highlightSubmissionId, loading, submissions, onHighlightHandled]);

  const pendingIds = submissions
    .filter((s) => s.status === "PENDING")
    .map((s) => s.submissionId);
  const allSelected = pendingIds.length > 0 && pendingIds.every((id) => selectedIds.has(id));

  const toggleSelect = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    setSelectedIds(allSelected ? new Set() : new Set(pendingIds));
  };

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

  const handleImportSelected = async () => {
    const ids = Array.from(selectedIds);
    if (ids.length === 0) return;
    if (!window.confirm(`Import ${ids.length} selected student(s) into PRMS?`)) return;
    setImporting(true);
    setError(null);
    setNotice(null);
    try {
      const res = await importSelectedStudents(ids);
      const { created, failed } = res.data;
      setNotice(
        `${created} student(s) imported` +
        (failed > 0 ? ` — ${failed} failed (see reasons below).` : ".")
      );
      setSelectedIds(new Set());
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Bulk import failed.");
    } finally {
      setImporting(false);
    }
  };

  const handleImportOne = async (submission) => {
    setError(null);
    setNotice(null);
    try {
      await includeStudentSubmission(submission.submissionId);
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to import this submission.");
    }
  };

  const handleFlag = (submission) => {
    setFlagTarget(submission);
    setFlagComment("");
  };

  const handleFlagCancel = () => {
    setFlagTarget(null);
    setFlagComment("");
  };

  const handleFlagSubmit = async () => {
    if (!flagComment.trim()) return;
    setFlagging(true);
    setError(null);
    setNotice(null);
    try {
      await flagStudentSubmission(flagTarget.submissionId, flagComment.trim());
      setFlagTarget(null);
      setFlagComment("");
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to flag this submission.");
    } finally {
      setFlagging(false);
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
        <div className="mb-3 d-flex gap-2">
          <button
            className="btn btn-success"
            onClick={handleImportAll}
            disabled={importing}
          >
            {importing ? "Importing…" : `Import All Students (${submissions.length})`}
          </button>
          <button
            className="btn btn-outline-success"
            onClick={handleImportSelected}
            disabled={importing || selectedIds.size === 0}
          >
            {importing ? "Importing…" : `Import Selected (${selectedIds.size})`}
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
                <th>
                  {tab === "PENDING" && (
                    <input
                      type="checkbox"
                      checked={allSelected}
                      onChange={toggleSelectAll}
                      disabled={pendingIds.length === 0}
                      title="Select all pending"
                    />
                  )}
                </th>
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
                <tr key={s.submissionId} id={`submission-${s.submissionId}`}>
                  <td>
                    {s.status === "PENDING" && (
                      <input
                        type="checkbox"
                        checked={selectedIds.has(s.submissionId)}
                        onChange={() => toggleSelect(s.submissionId)}
                      />
                    )}
                  </td>
                  <td>{s.submissionId}</td>
                  <td style={{ fontSize: "0.82rem" }}>{s.submittedAt || "—"}</td>
                  <td className="fw-semibold">
                    {s.fullName || "—"}
                    {s.failureReason && (
                      <div className="text-danger" style={{ fontSize: "0.78rem" }}>
                        ⚠ {s.failureReason}
                      </div>
                    )}
                    {s.flagged && (
                      <div className="text-warning-emphasis" style={{ fontSize: "0.78rem" }}>
                        🚩 Flagged: {s.flagComment}
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
                      <>
                        <button
                          className="btn btn-sm btn-success me-2"
                          onClick={() => handleImportOne(s)}
                        >
                          Import
                        </button>
                        <button
                          className="btn btn-sm btn-outline-warning me-2"
                          onClick={() => handleFlag(s)}
                          disabled={s.flagged}
                        >
                          {s.flagged ? "🚩 Flagged" : "Flag"}
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleReject(s)}
                        >
                          Reject
                        </button>
                      </>
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

      {flagTarget && (
        <div
          className="modal show d-block"
          tabIndex="-1"
          style={{ backgroundColor: "rgba(0,0,0,0.5)" }}
          onClick={handleFlagCancel}
        >
          <div className="modal-dialog" onClick={(e) => e.stopPropagation()}>
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">
                  Flag {flagTarget.fullName || flagTarget.email}'s registration
                </h5>
                <button type="button" className="btn-close" onClick={handleFlagCancel}></button>
              </div>
              <div className="modal-body">
                <label className="form-label">
                  What needs to be corrected? This will be emailed to the student.
                </label>
                <textarea
                  className="form-control"
                  rows="4"
                  autoFocus
                  value={flagComment}
                  onChange={(e) => setFlagComment(e.target.value)}
                  placeholder="e.g. Your resume link is broken, please resubmit."
                />
              </div>
              <div className="modal-footer">
                <button className="btn btn-outline-secondary" onClick={handleFlagCancel}>
                  Cancel
                </button>
                <button
                  className="btn btn-warning"
                  onClick={handleFlagSubmit}
                  disabled={flagging || !flagComment.trim()}
                >
                  {flagging ? "Flagging…" : "Flag & Notify"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default StudentFormSubmissions;
