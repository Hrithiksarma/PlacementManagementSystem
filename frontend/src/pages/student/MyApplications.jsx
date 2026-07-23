import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import {
  getStudentApplications,
  acceptOffer,
  rejectOffer,
  withdrawApplication,
  declineOffer,
  getWithdrawalPenaltyPreview,
  getPenaltyStatus,
} from "../../services/studentPortalService";
import "./MyApplications.css";

const PIPELINE = [
  "Applied", "First Round", "Interview Scheduled",
  "Selected", "Offer Accepted",
];

// "Selected" IS the job offer (no separate Offer Released stage) — it's
// where the Accept/Reject decision banner appears.
const STATUS_META = {
  Applied:               { color: "#2563eb", bg: "#eff6ff",  icon: "📝" },
  "First Round":         { color: "#7c3aed", bg: "#f5f3ff",  icon: "⭐" },
  "Interview Scheduled": { color: "#d97706", bg: "#fffbeb",  icon: "📅" },
  Selected:              { color: "#78350f", bg: "#fffbeb",  icon: "📩" },
  "Offer Accepted":      { color: "#16a34a", bg: "#f0fdf4",  icon: "✅" },
  "Offer Rejected":      { color: "#ea580c", bg: "#fff7ed",  icon: "↩️" },
  "Offer Declined":      { color: "#be123c", bg: "#fff1f2",  icon: "⛔" },
  Rejected:              { color: "#dc2626", bg: "#fef2f2",  icon: "❌" },
  Withdrawn:             { color: "#64748b", bg: "#f8fafc",  icon: "🚫" },
};

const TIER_COLOR = {
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

function StatusTimeline({ status }) {
  const isRejected      = status === "Rejected";
  const isWithdrawn     = status === "Withdrawn";
  const isOfferRejected = status === "Offer Rejected";
  const isOfferDeclined = status === "Offer Declined";
  const isTerminal      = isRejected || isWithdrawn || isOfferRejected || isOfferDeclined;
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
      {isOfferDeclined && (
        <div className="ma-timeline-step">
          <div className="ma-step-dot" style={{ background: "#be123c", color: "#fff", border: "none" }}>⛔</div>
          <div className="ma-step-label" style={{ color: "#be123c" }}>Offer Declined</div>
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
  const [penalty, setPenalty] = useState(null);   // current bar status (banner)
  // { app, preview, mode: "withdraw" | "decline", acknowledged } — null = closed
  const [confirmModal, setConfirmModal] = useState(null);

  const loadPenaltyStatus = () => {
    getPenaltyStatus()
      .then((res) => setPenalty(res.data))
      .catch(() => {});
  };

  useEffect(() => {
    getStudentApplications()
      .then((res) => { setApps(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load applications."); setLoading(false); });
    loadPenaltyStatus();
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

  // Opens the confirmation modal with the server-computed penalty for this app.
  const openConfirm = async (app, mode) => {
    setActing(app.applicationId);
    try {
      const res = await getWithdrawalPenaltyPreview(app.applicationId);
      setConfirmModal({ app, preview: res.data, mode, acknowledged: false });
    } catch {
      setToast({ type: "error", msg: "Could not determine the withdrawal penalty. Try again." });
    } finally {
      setActing(null);
    }
  };

  const handleConfirmedAction = async () => {
    const { app, mode } = confirmModal;
    setActing(app.applicationId);
    try {
      const res = mode === "decline" ? await declineOffer(app.applicationId)
                : mode === "reject"  ? await rejectOffer(app.applicationId)
                :                      await withdrawApplication(app.applicationId);
      const newStatus = mode === "decline" ? "Offer Declined"
                       : mode === "reject"  ? "Offer Rejected"
                       :                      "Withdrawn";
      setApps((prev) =>
        prev.map((a) => a.applicationId === app.applicationId ? { ...a, status: newStatus } : a)
      );
      setToast({ type: "success", msg: res.data?.message ?? "Done." });
      loadPenaltyStatus();
    } catch (err) {
      setToast({ type: "error", msg: err.response?.data?.message ?? "Action failed." });
    } finally {
      setActing(null);
      setConfirmModal(null);
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

        {/* ── Active penalty banner ──────────────────────────────────── */}
        {penalty?.barred && (
          <div className="alert alert-warning d-flex align-items-center py-2">
            <span className="me-2">⚠️</span>
            <span className="flex-grow-1">
              {penalty.message}
              {penalty.disciplinaryReferral && (
                <strong> Your record has been flagged for disciplinary referral.</strong>
              )}
            </span>
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
              // "Selected" IS the job offer — that stage shows Accept/Reject
              // instead of a Withdraw button.
              const isPending = app.status === "Selected";
              const canWithdraw = ["Applied", "First Round", "Interview Scheduled"]
                .includes(app.status);

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
                          {TIER_LABEL[app.companyTier] ?? app.companyTier}
                        </span>
                      )}
                      <span className="ma-status-badge"
                            style={{ background: meta.bg, color: meta.color }}>
                        {meta.icon} {app.status}
                      </span>
                      {canWithdraw && (
                        <button
                          className="btn btn-sm btn-outline-secondary"
                          style={{ fontSize: "0.75rem" }}
                          disabled={acting === app.applicationId}
                          onClick={() => openConfirm(app, "withdraw")}
                        >
                          {acting === app.applicationId ? "…" : "🚫 Withdraw"}
                        </button>
                      )}
                      {app.status === "Offer Accepted" && (
                        <button
                          className="btn btn-sm btn-outline-danger"
                          style={{ fontSize: "0.75rem" }}
                          disabled={acting === app.applicationId}
                          onClick={() => openConfirm(app, "decline")}
                        >
                          {acting === app.applicationId ? "…" : "🚫 Withdraw"}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Selected — this IS the job offer, decision banner */}
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
                          onClick={() => openConfirm(app, "reject")}
                        >
                          {acting === app.applicationId ? "…" : "✕ Reject"}
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
        {/* ── Penalty confirmation modal ──────────────────────────────── */}
        {confirmModal && (() => {
          const { app, preview, mode, acknowledged } = confirmModal;
          const blocked   = preview.penaltyType === "BLOCKED";
          const hasPenalty = !blocked && preview.penaltyType !== "NONE";
          const isPermanent = preview.penaltyType === "PERMANENT_BAN";
          const verb = mode === "reject" ? "Reject Offer" : "Withdraw";

          return (
            <div style={{
              position: "fixed", inset: 0, background: "rgba(15,23,42,0.55)",
              display: "flex", alignItems: "center", justifyContent: "center", zIndex: 1050,
            }}>
              <div style={{
                background: "#fff", borderRadius: 12, padding: "22px 24px",
                width: "min(480px, 92vw)", boxShadow: "0 20px 50px rgba(0,0,0,0.3)",
              }}>
                <h5 className="mb-1">
                  {blocked ? "Not allowed" : `${verb} — ${app.companyName ?? "this drive"}?`}
                </h5>
                <div className="text-muted mb-3" style={{ fontSize: "0.85rem" }}>
                  {app.roleOffered} · current stage: <strong>{app.status}</strong>
                </div>

                <div style={{
                  background: hasPenalty ? "#fef2f2" : "#f0fdf4",
                  border: `1px solid ${hasPenalty ? "#fecaca" : "#bbf7d0"}`,
                  color: hasPenalty ? "#991b1b" : "#166534",
                  borderRadius: 8, padding: "12px 14px", fontSize: "0.88rem",
                }}>
                  {hasPenalty ? "⚠️ " : "ℹ️ "}{preview.message}
                </div>

                {isPermanent && !blocked && (
                  <label className="d-flex align-items-start gap-2 mt-3" style={{ fontSize: "0.85rem" }}>
                    <input
                      type="checkbox"
                      checked={acknowledged}
                      onChange={(e) =>
                        setConfirmModal({ ...confirmModal, acknowledged: e.target.checked })}
                    />
                    <span>
                      I understand this <strong>permanently</strong> ends my participation in
                      campus placements and refers my record for disciplinary action.
                    </span>
                  </label>
                )}

                <div className="d-flex justify-content-end gap-2 mt-4">
                  <button className="btn btn-sm btn-outline-secondary"
                          onClick={() => setConfirmModal(null)}>
                    Cancel
                  </button>
                  {!blocked && (
                    <button
                      className={`btn btn-sm ${hasPenalty ? "btn-danger" : "btn-primary"}`}
                      disabled={acting === app.applicationId || (isPermanent && !acknowledged)}
                      onClick={handleConfirmedAction}
                    >
                      {acting === app.applicationId ? "…" : `Confirm ${verb}`}
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })()}
      </div>
    </Layout>
  );
}

export default MyApplications;
