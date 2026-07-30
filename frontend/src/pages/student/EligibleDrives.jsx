import { useEffect, useRef, useState } from "react";
import { useLocation } from "react-router-dom";
import Layout from "../../components/Layout";
import { getEligibleDrives, applyToDrive, getPenaltyStatus } from "../../services/studentPortalService";
import "./EligibleDrives.css";

const TIER_COLOR = {
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

const DRIVE_STATUS_BADGE = {
  "Upcoming":  { bg: "#eff6ff", color: "#1d4ed8" },
  "Active":    { bg: "#f0fdf4", color: "#15803d" },
  "Completed": { bg: "#f1f5f9", color: "#475569" },
};

function TierBadge({ tier }) {
  const color = TIER_COLOR[tier] ?? "#374151";
  return (
    <span className="ed-tier" style={{ background: color }}>
      {tier ? (TIER_LABEL[tier] ?? tier) : "—"}
    </span>
  );
}

function DriveStatusBadge({ status }) {
  const s = DRIVE_STATUS_BADGE[status];
  if (!s) return null;
  return (
    <span className="ed-drive-status" style={{ background: s.bg, color: s.color }}>
      {status}
    </span>
  );
}

function EligibleDrives() {
  const [drives,      setDrives]      = useState([]);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState(null);
  const [applying,    setApplying]    = useState(null);
  const [toast,       setToast]       = useState(null);
  const [filterTier,  setFilterTier]  = useState("All");
  const [filterStatus,setFilterStatus]= useState("All");

  const [penalty, setPenalty] = useState(null);

  const location       = useLocation();
  const highlightId    = location.state?.highlightDriveId ?? null;
  const highlightedRef = useRef(false);

  useEffect(() => {
    if (!highlightId || highlightedRef.current || drives.length === 0) return;
    highlightedRef.current = true;
    const el = document.getElementById(`drive-${highlightId}`);
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "center" });
      el.classList.add("ed-card-highlight");
      setTimeout(() => el.classList.remove("ed-card-highlight"), 2500);
    }
  }, [drives, highlightId]);

  const loadDrives = () => {
    setLoading(true);
    getEligibleDrives()
      .then((res) => { setDrives(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load drives."); setLoading(false); });
    getPenaltyStatus()
      .then((res) => setPenalty(res.data))
      .catch(() => {});
  };

  useEffect(() => { loadDrives(); }, []);

  const showToast = (msg, type = "success") => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3500);
  };

  const handleApply = async (driveId) => {
    setApplying(driveId);
    try {
      await applyToDrive(driveId);
      showToast("Application submitted successfully!");
      loadDrives();
    } catch (err) {
      const data = err?.response?.data;
      const msg =
        (typeof data === "string" && data ? data : null) ||
        data?.message ||
        data?.error ||
        "Failed to apply. Please try again.";
      showToast(String(msg), "error");
    } finally {
      setApplying(null);
    }
  };

  const tiers    = ["All", ...new Set(drives.map((d) => d.companyTier).filter(Boolean))];
  const statuses = ["All", ...new Set(drives.map((d) => d.driveStatus).filter(Boolean))];

  const filtered = drives
    .filter((d) => filterTier   === "All" || d.companyTier  === filterTier)
    .filter((d) => filterStatus === "All" || d.driveStatus  === filterStatus);

  // Hard-blocked = academic (CGPA/backlog) miss or an active penalty bar —
  // the card is disabled and shows its reason up front. A drive blocked only
  // by the one-time upgradation policy stays fully clickable; the policy
  // explanation surfaces as a toast if/when the apply attempt is rejected.
  const isHardBlocked = (d) => d.eligible === false && !d.upgradationBlocked;

  const openCount       = filtered.filter((d) => !isHardBlocked(d) && !d.alreadyApplied && d.driveStatus !== "Completed").length;
  const appliedCount    = filtered.filter((d) => d.alreadyApplied).length;
  const ineligibleCount = filtered.filter((d) => isHardBlocked(d) && !d.alreadyApplied).length;

  const formatDate = (ds) => ds
    ? new Date(ds).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" })
    : "—";

  if (loading) {
    return (
      <Layout>
        <div className="ed-container">
          {[1, 2, 3].map((i) => (
            <div key={i} className="ed-skeleton" style={{ height: 120 }} />
          ))}
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  return (
    <Layout>
      <div className="ed-container">

        {/* Toast */}
        {toast && (
          <div className={`ed-toast ${toast.type === "error" ? "ed-toast-error" : "ed-toast-ok"}`}>
            {toast.msg}
          </div>
        )}

        {/* ── Active penalty banner ──────────────────────────────────── */}
        {penalty?.barred && (
          <div className="alert alert-warning d-flex align-items-center py-2 mb-3">
            <span className="me-2">⚠️</span>
            <span className="flex-grow-1">{penalty.message}</span>
          </div>
        )}

        {/* ── Header ─────────────────────────────────────────────────── */}
        <div className="ed-page-header">
          <div>
            <h2 className="ed-page-title">Placement Drives</h2>
            <p className="ed-page-sub">
              All active drives — your eligibility is shown on each card
            </p>
          </div>
          <div className="ed-summary-pills">
            <span className="ed-sum-pill ed-sum-open">{openCount} Open</span>
            <span className="ed-sum-pill ed-sum-applied">{appliedCount} Applied</span>
            {ineligibleCount > 0 && (
              <span className="ed-sum-pill ed-sum-ineligible">{ineligibleCount} Not Eligible</span>
            )}
          </div>
        </div>

        {/* ── Filters ────────────────────────────────────────────────── */}
        <div className="ed-filter-bar">
          <div className="ed-filter-group">
            <label className="ed-filter-label">Tier</label>
            <select className="ed-filter-select" value={filterTier}
              onChange={(e) => setFilterTier(e.target.value)}>
              {tiers.map((t) => <option key={t} value={t}>{t === "All" ? t : (TIER_LABEL[t] ?? t)}</option>)}
            </select>
          </div>
          <div className="ed-filter-group">
            <label className="ed-filter-label">Status</label>
            <select className="ed-filter-select" value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}>
              {statuses.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          {(filterTier !== "All" || filterStatus !== "All") && (
            <button className="ed-filter-clear"
              onClick={() => { setFilterTier("All"); setFilterStatus("All"); }}>
              Clear ✕
            </button>
          )}
        </div>

        {/* ── Drive Cards ─────────────────────────────────────────────── */}
        {filtered.length === 0 ? (
          <div className="ed-empty">
            <span className="ed-empty-icon">🎯</span>
            <p>No drives found for the selected filters.</p>
          </div>
        ) : (
          <div className="ed-cards">
            {filtered.map((d) => {
              const isCompleted = d.driveStatus === "Completed";
              const blocked     = isHardBlocked(d);

              return (
                <div key={d.driveId}
                  id={`drive-${d.driveId}`}
                  className={`ed-card${d.alreadyApplied ? " ed-card-applied" : ""}${(blocked && !d.alreadyApplied) ? " ed-card-ineligible" : ""}`}>

                  {/* Card header */}
                  <div className="ed-card-head">
                    <div className="ed-card-company">{d.companyName ?? "—"}</div>
                    <div className="ed-card-badges">
                      {d.companyTier && <TierBadge tier={d.companyTier} />}
                      {d.driveStatus && <DriveStatusBadge status={d.driveStatus} />}
                      {d.alreadyApplied && (
                        <span className="ed-applied-badge">✓ Applied</span>
                      )}
                    </div>
                  </div>

                  {/* Role & package */}
                  <div className="ed-card-role">{d.roleOffered}</div>
                  {d.companyWebsite && (
                    <div className="ed-card-website">{d.companyWebsite}</div>
                  )}

                  {/* Ineligibility reason — only for hard blocks (academic / penalty).
                      An upgradation-policy block stays quiet here; its explanation
                      surfaces as a toast if the student actually tries to apply. */}
                  {blocked && !d.alreadyApplied && d.eligibilityReason && (
                    <div className="ed-ineligible-reason">
                      ⚠ {d.eligibilityReason}
                    </div>
                  )}

                  {/* Criteria chips */}
                  <div className="ed-criteria-row">
                    {d.packageLpa != null && (
                      <span className="ed-crit ed-crit-pkg">
                        💰 {d.packageLpa} LPA
                      </span>
                    )}
                    {d.minCgpa != null && (
                      <span className="ed-crit">Min CGPA {d.minCgpa}</span>
                    )}
                    {d.maxBacklogs != null && (
                      <span className="ed-crit">Max Backlogs {d.maxBacklogs}</span>
                    )}
                    {d.driveDate && (
                      <span className="ed-crit">📅 {formatDate(d.driveDate)}</span>
                    )}
                  </div>

                  {/* Action */}
                  <div className="ed-card-footer">
                    {d.alreadyApplied ? (
                      <span className="ed-applied-msg">✅ Application Submitted</span>
                    ) : isCompleted ? (
                      <span className="ed-closed-msg">🔒 Applications Closed</span>
                    ) : (
                      <button
                        className="ed-apply-btn"
                        disabled={blocked || applying === d.driveId}
                        onClick={() => handleApply(d.driveId)}
                      >
                        {applying === d.driveId ? "Submitting…" : "Apply Now →"}
                      </button>
                    )}
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

export default EligibleDrives;
