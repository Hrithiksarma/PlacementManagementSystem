import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import { getEligibleDrives, applyToDrive } from "../../services/studentPortalService";
import "./EligibleDrives.css";

const TIER_COLOR = {
  "Super Dream": "#7c3aed",
  "Dream":       "#16a34a",
  "Normal":      "#2563eb",
};

const DRIVE_STATUS_BADGE = {
  "Upcoming":  { bg: "#eff6ff", color: "#1d4ed8" },
  "Active":    { bg: "#f0fdf4", color: "#15803d" },
  "Completed": { bg: "#f1f5f9", color: "#475569" },
};

function TierBadge({ tier }) {
  const color = TIER_COLOR[tier] ?? "#374151";
  return (
    <span className="ed-tier" style={{ background: color }}>
      {tier ?? "—"}
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

  const loadDrives = () => {
    setLoading(true);
    getEligibleDrives()
      .then((res) => { setDrives(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load drives."); setLoading(false); });
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

  const openCount       = filtered.filter((d) => (d.eligible ?? true) && !d.alreadyApplied && d.driveStatus !== "Completed").length;
  const appliedCount    = filtered.filter((d) => d.alreadyApplied).length;
  const ineligibleCount = filtered.filter((d) => d.eligible === false && !d.alreadyApplied).length;

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
              {tiers.map((t) => <option key={t} value={t}>{t}</option>)}
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
              const isEligible  = d.eligible ?? true;

              return (
                <div key={d.driveId}
                  className={`ed-card${d.alreadyApplied ? " ed-card-applied" : ""}${(!isEligible && !d.alreadyApplied) ? " ed-card-ineligible" : ""}`}>

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

                  {/* Ineligibility reason */}
                  {!isEligible && !d.alreadyApplied && d.eligibilityReason && (
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
                        disabled={!isEligible || applying === d.driveId}
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
