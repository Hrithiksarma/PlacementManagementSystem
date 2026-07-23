import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import Layout from "../components/Layout";
import StudentFormSubmissions from "./StudentFormSubmissions";
import {
  getSubmissions,
  getCompaniesWithoutSubmission,
  syncSubmissions,
  includeSubmission,
  rejectSubmission,
} from "../services/formService";

const TABS = [
  { key: "PENDING",       label: "Submitted (Pending)" },
  { key: "INCLUDED",      label: "Included" },
  { key: "REJECTED",      label: "Rejected" },
  { key: "NOT_SUBMITTED", label: "Not Submitted" },
];

function FormSubmissions() {
  const [view, setView]                 = useState("company"); // "company" | "student"
  const [tab, setTab]                   = useState("PENDING");
  const [submissions, setSubmissions]   = useState([]);
  const [notSubmitted, setNotSubmitted] = useState([]);
  const [loading, setLoading]           = useState(false);
  const [syncing, setSyncing]           = useState(false);
  const [error, setError]               = useState(null);
  const [notice, setNotice]             = useState(null);

  const loadTab = useCallback(async (which) => {
    setLoading(true);
    setError(null);
    try {
      if (which === "NOT_SUBMITTED") {
        const res = await getCompaniesWithoutSubmission();
        setNotSubmitted(res.data);
      } else {
        const res = await getSubmissions(which);
        setSubmissions(res.data);
      }
    } catch (err) {
      console.error("Error loading form submissions:", err);
      setError(err.response?.data?.message ?? "Failed to load form submissions.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    if (view === "company") loadTab(tab);
  }, [tab, loadTab, view]);

  const handleSync = async () => {
    setSyncing(true);
    setError(null);
    setNotice(null);
    try {
      const res = await syncSubmissions();
      setNotice(`Refreshed from Google Form — ${res.data.newSubmissions} new submission(s).`);
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Sync failed. Check the Google Form configuration.");
    } finally {
      setSyncing(false);
    }
  };

  const handleInclude = async (submission) => {
    if (!window.confirm(`Include ${submission.companyName}'s drive into PRMS?`)) return;
    setError(null);
    setNotice(null);
    try {
      const res = await includeSubmission(submission.submissionId);
      setNotice(
        `${submission.companyName} included — drive #${res.data.driveId} created and now visible everywhere.`
      );
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to include this submission.");
    }
  };

  const handleReject = async (submission) => {
    if (!window.confirm(`Reject ${submission.companyName}'s submission?`)) return;
    setError(null);
    setNotice(null);
    try {
      await rejectSubmission(submission.submissionId);
      await loadTab(tab);
    } catch (err) {
      setError(err.response?.data?.message ?? "Failed to reject this submission.");
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("en-IN", {
      day: "2-digit", month: "short", year: "numeric",
    });
  };

  return (
    <Layout>
      {/* Top-level view switcher: Company Drives | Student Registrations */}
      <ul className="nav nav-tabs mb-3">
        <li className="nav-item">
          <button
            className={`nav-link ${view === "company" ? "active" : ""}`}
            onClick={() => setView("company")}
          >
            🏢 Company Drives
          </button>
        </li>
        <li className="nav-item">
          <button
            className={`nav-link ${view === "student" ? "active" : ""}`}
            onClick={() => setView("student")}
          >
            🎓 Student Registrations
          </button>
        </li>
      </ul>

      {view === "student" ? (
        <StudentFormSubmissions />
      ) : (
      <>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="mb-0">Forms</h2>
        <div className="d-flex gap-2 align-items-center">
          <span className="text-muted" style={{ fontSize: "0.82rem" }}>
            📝 Companies submit drives through the Google Form
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

      {/* Tabs */}
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

      {loading ? (
        <p className="text-muted">Loading…</p>
      ) : tab === "NOT_SUBMITTED" ? (
        /* Companies already in PRMS that never filled the form */
        notSubmitted.length === 0 ? (
          <div className="alert alert-info">
            Every company in PRMS has submitted the form.
          </div>
        ) : (
          <>
            <p className="text-muted mb-2">
              <strong>{notSubmitted.length}</strong> company(ies) in PRMS have not submitted the form yet.
            </p>
            <table className="table table-striped table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>ID</th>
                  <th>Company</th>
                  <th>Sector</th>
                  <th>Tier</th>
                  <th>Website</th>
                </tr>
              </thead>
              <tbody>
                {notSubmitted.map((c) => (
                  <tr key={c.companyId}>
                    <td>{c.companyId}</td>
                    <td>{c.companyName}</td>
                    <td>{c.sector ?? "—"}</td>
                    <td>{c.tier}</td>
                    <td>
                      {c.website
                        ? <a href={c.website} target="_blank" rel="noreferrer">{c.website}</a>
                        : "—"}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        )
      ) : submissions.length === 0 ? (
        <div className="alert alert-warning">
          No {tab.toLowerCase()} submissions. Click "Refresh from Google Form" to pull the latest responses.
        </div>
      ) : (
        <>
          <p className="text-muted mb-2">
            Showing <strong>{submissions.length}</strong> submission(s)
          </p>
          <table className="table table-striped table-bordered table-hover">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Submitted</th>
                <th>Company</th>
                <th>Tier</th>
                <th>Role</th>
                <th>Package (LPA)</th>
                <th>Type</th>
                <th>Drive Date</th>
                <th>Min CGPA</th>
                <th>Max Backlogs</th>
                <th>HR Contact</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {submissions.map((s) => (
                <tr key={s.submissionId}>
                  <td>{s.submissionId}</td>
                  <td style={{ fontSize: "0.82rem" }}>{s.submittedAt || "—"}</td>
                  <td className="fw-semibold">{s.companyName}</td>
                  <td>{s.tier || "—"}</td>
                  <td>{s.roleOffered || "—"}</td>
                  <td>{s.packageLpa ?? "—"}</td>
                  <td>{s.driveType || "—"}</td>
                  <td>{formatDate(s.driveDate)}</td>
                  <td>{s.minCgpa ?? "—"}</td>
                  <td>{s.maxBacklogs ?? "—"}</td>
                  <td style={{ fontSize: "0.82rem" }}>
                    {s.hrName
                      ? <>{s.hrName}<br />{s.hrEmail}</>
                      : "—"}
                  </td>
                  <td>
                    {s.status === "PENDING" ? (
                      <>
                        <button
                          className="btn btn-sm btn-success me-2"
                          onClick={() => handleInclude(s)}
                        >
                          Include Company
                        </button>
                        <button
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => handleReject(s)}
                        >
                          Reject
                        </button>
                      </>
                    ) : s.status === "INCLUDED" ? (
                      <Link to="/admin/drives" className="badge bg-success text-decoration-none">
                        ✓ Drive #{s.driveId}
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
      )}
    </Layout>
  );
}

export default FormSubmissions;
