import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import {
  getStudentApplications,
  acceptOffer,
  rejectOffer,
} from "../../services/studentPortalService";
import "./MyApplications.css";

const PIPELINE = [
  "Applied", "Shortlisted", "Interview Scheduled",
  "Selected", "Offer Released", "Offer Accepted",
];

const STATUS_META = {
  Applied:               { color: "#2563eb", bg: "#eff6ff",  icon: "📝" },
  Shortlisted:           { color: "#7c3aed", bg: "#f5f3ff",  icon: "⭐" },
  "Interview Scheduled": { color: "#d97706", bg: "#fffbeb",  icon: "📅" },
  Selected:              { color: "#0891b2", bg: "#ecfeff",  icon: "🎯" },
  "Offer Released":      { color: "#78350f", bg: "#fffbeb",  icon: "📩" },
  "Offer Accepted":      { color: "#16a34a", bg: "#f0fdf4",  icon: "✅" },
  "Offer Rejected":      { color: "#ea580c", bg: "#fff7ed",  icon: "↩️" },
  Rejected:              { color: "#dc2626", bg: "#fef2f2",  icon: "❌" },
  Withdrawn:             { color: "#64748b", bg: "#f8fafc",  icon: "🚫" },
};

const TIER_COLOR = {
  "Super Dream": "#7c3aed",
  "Dream":       "#16a34a",
  "Normal":      "#2563eb",
};

function StatusTimeline({ status }) {
  const isRejected      = status === "Rejected";
  const isWithdrawn     = status === "Withdrawn";
  const isOfferRejected = status === "Offer Rejected";
  const isTerminal      = isRejected || isWithdrawn || isOfferRejected;
  const activeIdx       = PIPELINE.indexOf(status);

  return (
    <div className="ma-timeline">
      {PIPELINE.map((step, i) => {
        const isDone    = !isTerminal && i < activeIdx;
        const isCurrent = !isTerminal && i === activeIdx;

        return (
          <div key={step} className="ma-timeline-step">
            <div className={`ma-step-dot ${
              isCurrent ? "ma-dot-active" :
              isDone    ? "ma-dot-done"   :
              isTerminal && i === 0 ? "ma-dot-done" : "ma-dot-future"
            }`}>
              {isDone || (isTerminal && i === 0) ? "✓" : i + 1}
            </div>
            {i < PIPELINE.length - 1 && (
              <div className={`ma-step-line ${isDone ? "ma-line-done" : "ma-line-future"}`} />
            )}
            <div className={`ma-step-label ${isCurrent ? "ma-label-active" : !isDone ? "ma-label-future" : ""}`}>
              {step}
            </div>
          </div>
        );
      })}

      {isRejected && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot ma-dot-rejected">✕</div>
          <div className="ma-step-label ma-label-rejected">Rejected</div>
        </div>
      )}
      {isOfferRejected && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot" style={{ background: "#ea580c", color: "#fff", border: "none" }}>↩</div>
          <div className="ma-step-label" style={{ color: "#ea580c" }}>Offer Rejected</div>
        </div>
      )}
      {isWithdrawn && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot" style={{ background: "#94a3b8", color: "#fff", border: "none" }}>–</div>
          <div className="ma-step-label" style={{ color: "#64748b" }}>Withdrawn</div>
        </div>
      )}
    </div>
  );
}

function formatDate(ds) {
  if (!ds) return "—";
  return new Date(ds).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function MyApplications() {
  const [apps,    setApps]    = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);
  const [filter,  setFilter]  = useState("All");
  const [acting,  setActing]  = useState(null);
  const [toast,   setToast]   = useState(null);

  useEffect(() => {
    getStudentApplications()
      .then((res) => { setApps(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load applications."); setLoading(false); });
  }, []);

  useEffect(() => {
    if (!toast) return;
    const t = setTimeout(() => setToast(null), 4500);
    return () => clearTimeout(t);
  }, [toast]);

  const handleAccept = async (appId) => {
    setActing(appId);
    try {
      await acceptOffer(appId);
      setApps((prev) =>
        prev.map((a) => a.applicationId === appId ? { ...a, status: "Offer Accepted" } : a)
      );
      setToast({ type: "success", msg: "Offer accepted! Congratulations on your placement." });
    } catch (err) {
      setToast({ type: "error", msg: err.response?.data?.message ?? "Failed to accept offer." });
    } finally {
      setActing(null);
    }
  };

  const handleReject = async (appId) => {
    if (!window.confirm("Reject this offer? This cannot be undone.")) return;
    setActing(appId);
    try {
      await rejectOffer(appId);
      setApps((prev) =>
        prev.map((a) => a.applicationId === appId ? { ...a, status: "Offer Rejected" } : a)
      );
      setToast({ type: "success", msg: "Offer rejected. You can continue applying to other drives." });
    } catch (err) {
      setToast({ type: "error", msg: err.response?.data?.message ?? "Failed to reject offer." });
    } finally {
      setActing(null);
    }
  };

  const displayed = filter === "All" ? apps : apps.filter((a) => a.status === filter);

  const counts = Object.fromEntries(
    Object.keys(STATUS_META).map((s) => [s, apps.filter((a) => a.status === s).length])
  );

  if (loading) {
    return (
      <Layout>
        <div className="ma-container">
          {[1, 2, 3].map((i) => <div key={i} className="ma-skeleton" style={{ height: 160 }} />)}
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  return (
    <Layout>
      <div className="ma-container">

        {/* ── Toast ──────────────────────────────────────────────────── */}
        {toast && (
          <div className={`alert alert-${toast.type === "success" ? "success" : "danger"} d-flex align-items-center py-2`}>
            <span className="me-2">{toast.type === "success" ? "✅" : "⚠️"}</span>
            <span className="flex-grow-1">{toast.msg}</span>
            <button className="btn-close btn-close-sm" onClick={() => setToast(null)} />
          </div>
        )}

        {/* ── Header ─────────────────────────────────────────────────── */}
        <div className="ma-page-header">
          <div>
            <h2 className="ma-page-title">My Applications</h2>
            <p className="ma-page-sub">{apps.length} total application{apps.length !== 1 ? "s" : ""}</p>
          </div>
        </div>

        {/* ── Filter pills ────────────────────────────────────────────── */}
        <div className="ma-filter-row">
          {["All", ...Object.keys(STATUS_META)].map((s) => {
            const meta  = STATUS_META[s];
            const count = s === "All" ? apps.length : (counts[s] ?? 0);
            return (
              <button
                key={s}
                className={`ma-filter-pill ${filter === s ? "ma-filter-active" : ""}`}
                style={filter === s && meta
                  ? { background: meta.bg, color: meta.color, borderColor: meta.color }
                  : {}}
                onClick={() => setFilter(s)}
              >
                {meta?.icon} {s} ({count})
              </button>
            );
          })}
        </div>

        {/* ── Cards ───────────────────────────────────────────────────── */}
        {displayed.length === 0 ? (
          <div className="ma-empty">
            <span className="ma-empty-icon">📋</span>
            <p>{filter === "All"
              ? "You haven't applied to any drives yet."
              : `No applications with status "${filter}".`}
            </p>
          </div>
        ) : (
          <div className="ma-cards">
            {displayed.map((app) => {
              const meta      = STATUS_META[app.status] ?? STATUS_META.Applied;
              const tierColor = TIER_COLOR[app.companyTier] ?? "#374151";
              const isPending = app.status === "Offer Released";

              return (
                <div key={app.applicationId}
                     className={`ma-card ${isPending ? "ma-card--offer-pending" : ""}`}>

                  {/* Header */}
                  <div className="ma-card-head">
                    <div className="ma-card-left">
                      <div className="ma-company">{app.companyName ?? "—"}</div>
                      <div className="ma-role">{app.roleOffered ?? "—"}</div>
                    </div>
                    <div className="ma-card-right">
                      {app.companyTier && (
                        <span className="ma-tier-badge" style={{ background: tierColor }}>
                          {app.companyTier}
                        </span>
                      )}
                      <span className="ma-status-badge"
                            style={{ background: meta.bg, color: meta.color }}>
                        {meta.icon} {app.status}
                      </span>
                    </div>
                  </div>

                  {/* Offer Released — decision banner */}
                  {isPending && (
                    <div className="ma-offer-banner">
                      <div className="ma-offer-banner-body">
                        <span className="ma-offer-banner-icon">📩</span>
                        <div>
                          <div className="ma-offer-banner-title">Offer Available — Action Required</div>
                          {app.offerDeadline
                            ? <div className="ma-offer-banner-deadline">Valid until {formatDate(app.offerDeadline)}</div>
                            : <div className="ma-offer-banner-deadline">Please respond at your earliest</div>}
                        </div>
                      </div>
                      <div className="ma-offer-actions">
                        <button
                          className="btn btn-sm ma-btn-accept"
                          disabled={acting === app.applicationId}
                          onClick={() => handleAccept(app.applicationId)}
                        >
                          {acting === app.applicationId ? "…" : "✓ Accept Offer"}
                        </button>
                        <button
                          className="btn btn-sm ma-btn-reject"
                          disabled={acting === app.applicationId}
                          onClick={() => handleReject(app.applicationId)}
                        >
                          ✕ Reject
                        </button>
                      </div>
                    </div>
                  )}

                  {/* Meta chips */}
                  <div className="ma-meta-row">
                    {app.packageLpa != null && (
                      <span className="ma-meta-chip ma-chip-pkg">💰 {app.packageLpa} LPA</span>
                    )}
                    <span className="ma-meta-chip">📝 Applied: {formatDate(app.applicationDate)}</span>
                    {app.driveDate && (
                      <span className="ma-meta-chip">📅 Drive: {formatDate(app.driveDate)}</span>
                    )}
                  </div>

                  {/* Timeline */}
                  <div className="ma-timeline-wrap">
                    <StatusTimeline status={app.status} />
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </Layout>
  );
}

export default MyApplications;
