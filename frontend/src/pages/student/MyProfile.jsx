import { useEffect, useState } from "react";
import Layout from "../../components/Layout";
import { getStudentProfile } from "../../services/studentPortalService";
import "./MyProfile.css";

const PLACEMENT_COLOR = {
  "Super Dream": "#7c3aed",
  "Dream":       "#16a34a",
  "Normal":      "#2563eb",
  "Unplaced":    "#475569",
};

function Field({ label, value, highlight }) {
  return (
    <div className="mp-field">
      <span className="mp-field-label">{label}</span>
      <span className={`mp-field-value${highlight ? " mp-field-highlight" : ""}`}>
        {value ?? "—"}
      </span>
    </div>
  );
}

function MyProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    getStudentProfile()
      .then((res) => { setProfile(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load profile."); setLoading(false); });
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="mp-container">
          <div className="mp-skeleton" style={{ height: 90 }} />
          <div className="mp-skeleton" style={{ height: 320 }} />
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  const placementColor = PLACEMENT_COLOR[profile?.placementTier] ?? "#475569";
  const initial        = profile?.name?.charAt(0)?.toUpperCase() ?? "S";

  return (
    <Layout>
      <div className="mp-container">

        {/* ── Profile Header ──────────────────────────────────────────── */}
        <div className="mp-header-card">
          <div className="mp-avatar" style={{ background: placementColor }}>{initial}</div>
          <div className="mp-header-body">
            <div className="mp-full-name">{profile?.name}</div>
            <div className="mp-sub-row">
              {profile?.rollNo && (
                <span className="mp-chip" style={{ fontFamily: "monospace", letterSpacing: "0.06em", fontWeight: 600 }}>
                  {profile.rollNo}
                </span>
              )}
              {profile?.branch   && <span className="mp-chip">{profile.branch}</span>}
              {profile?.program  && <span className="mp-chip">{profile.program}</span>}
              {profile?.batchYear && <span className="mp-chip">Batch {profile.batchYear}</span>}
            </div>
          </div>
          <div className="mp-header-right">
            <span className="mp-tier-badge" style={{ background: placementColor }}>
              {profile?.placementTier === "Unplaced" || !profile?.placementTier
                ? "Not Placed"
                : `Placed (${profile.placementTier})`}
            </span>
          </div>
        </div>

        {/* ── Details ─────────────────────────────────────────────────── */}
        <div className="mp-details-grid">

          <div className="mp-card">
            <div className="mp-card-title">📋 Personal Information</div>
            <div className="mp-fields">
              <Field label="Roll Number"   value={profile?.rollNo} />
              <Field label="Student ID"    value={`#${profile?.studentId}`} />
              <Field label="Full Name"     value={profile?.name} />
              <Field label="Email Address" value={profile?.email} />
              <Field label="Phone Number"  value={profile?.phone} />
            </div>
          </div>

          <div className="mp-card">
            <div className="mp-card-title">🎓 Academic Information</div>
            <div className="mp-fields">
              <Field label="Branch / Department" value={profile?.branch} />
              <Field label="Program"             value={profile?.program} />
              <Field label="Batch Year"          value={profile?.batchYear} />
              <Field label="CGPA" value={
                profile?.cgpa != null
                  ? `${profile.cgpa} / 10.0`
                  : "—"
              } highlight />
              <Field label="Active Backlogs" value={
                profile?.activeBacklogs != null
                  ? profile.activeBacklogs === 0
                    ? "0 ✓"
                    : String(profile.activeBacklogs)
                  : "—"
              } highlight={profile?.activeBacklogs === 0} />
            </div>
          </div>

          <div className="mp-card mp-card--full">
            <div className="mp-card-title">🏆 Placement Information</div>
            <div className="mp-placement-row">
              <div className="mp-placement-item">
                <div className="mp-pl-label">Placement Status</div>
                <div className="mp-pl-value">
                  <span className="mp-tier-badge" style={{ background: placementColor }}>
                    {profile?.placementTier === "Unplaced" || !profile?.placementTier
                      ? "Not Placed"
                      : `Placed (${profile.placementTier})`}
                  </span>
                </div>
              </div>
              <div className="mp-placement-item">
                <div className="mp-pl-label">Active Backlogs</div>
                <div className="mp-pl-value">
                  {profile?.activeBacklogs != null ? (
                    profile.activeBacklogs === 0
                      ? <span className="mp-elig-chip mp-elig-super">0 — Clear</span>
                      : <span className="mp-elig-chip mp-elig-block">
                          {profile.activeBacklogs} backlog{profile.activeBacklogs > 1 ? "s" : ""}
                        </span>
                  ) : "—"}
                </div>
              </div>
            </div>
            <div className="mp-readonly-note">
              🔒 Profile is read-only. Contact the Placement Cell to update your information.
            </div>
          </div>

        </div>
      </div>
    </Layout>
  );
}

export default MyProfile;