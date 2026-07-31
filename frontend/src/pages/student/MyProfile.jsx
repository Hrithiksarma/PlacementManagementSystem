import { useEffect, useState } from "react";
import { ClipboardList, GraduationCap, Trophy, Check, Lock, FileText, ExternalLink, Eye, EyeOff } from "lucide-react";
import Layout from "../../components/Layout";
import { getStudentProfile } from "../../services/studentPortalService";
import "./MyProfile.css";

const PLACEMENT_COLOR = {
  C:           "#7c3aed",
  B:           "#16a34a",
  A:           "#2563eb",
  "Unplaced":  "#475569",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

// Google Drive share URLs look like .../file/d/<ID>/view or ...?id=<ID>.
// Pull the file ID out so we can embed a preview iframe or thumbnail image.
function driveFileId(url) {
  if (!url) return null;
  const byPath = url.match(/\/d\/([-\w]{25,})/);
  if (byPath) return byPath[1];
  const byQuery = url.match(/[?&]id=([-\w]{25,})/);
  if (byQuery) return byQuery[1];
  const loose = url.match(/[-\w]{25,}/);
  return loose ? loose[0] : null;
}

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

function DocRow({ label, url }) {
  const [expanded, setExpanded] = useState(false);
  const fileId = driveFileId(url);

  return (
    <div className="mp-doc-row">
      <div className="mp-doc-row-head">
        <span className="mp-doc-row-label">{label}</span>
        {url ? (
          <div className="d-flex align-items-center gap-2">
            {fileId && (
              <button
                type="button"
                className="mp-doc-row-toggle"
                onClick={() => setExpanded((v) => !v)}
              >
                {expanded ? <EyeOff size={11} /> : <Eye size={11} />}
                {expanded ? "Hide" : "View"}
              </button>
            )}
            <a href={url} target="_blank" rel="noreferrer" className="mp-doc-row-link">
              Open in new tab
              <ExternalLink size={11} />
            </a>
          </div>
        ) : (
          <span className="mp-doc-row-empty">Not uploaded</span>
        )}
      </div>
      {expanded && url && fileId && (
        <div className="mp-doc-preview">
          <iframe
            title={`${label} preview`}
            src={`https://drive.google.com/file/d/${fileId}/preview`}
            width="100%"
            height="100%"
            style={{ border: "none" }}
          />
        </div>
      )}
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
  const photoFileId    = driveFileId(profile?.photoUrl);

  return (
    <Layout>
      <div className="mp-container">

        {/* ── Profile Header ──────────────────────────────────────────── */}
        <div className="mp-header-card">
          <div className="mp-avatar" style={{ background: placementColor }}>
            {photoFileId ? (
              <img
                className="mp-avatar-photo"
                src={`https://drive.google.com/thumbnail?id=${photoFileId}&sz=w200`}
                alt={profile?.name ?? "Profile photo"}
                onError={(e) => { e.currentTarget.style.display = "none"; }}
              />
            ) : (
              initial
            )}
          </div>
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
                : `Placed (${TIER_LABEL[profile.placementTier] ?? profile.placementTier})`}
            </span>
          </div>
        </div>

        {/* ── Details ─────────────────────────────────────────────────── */}
        <div className="mp-details-grid">

          <div className="mp-card">
            <div className="mp-card-title d-flex align-items-center gap-2">
              <ClipboardList size={16} />
              Personal Information
            </div>
            <div className="mp-fields">
              <Field label="Roll Number"   value={profile?.rollNo} />
              <Field label="Student ID"    value={`#${profile?.studentId}`} />
              <Field label="Full Name"     value={profile?.name} />
              <Field label="Email Address" value={profile?.email} />
              <Field label="Phone Number"  value={profile?.phone} />
            </div>
          </div>

          <div className="mp-card">
            <div className="mp-card-title d-flex align-items-center gap-2">
              <GraduationCap size={16} />
              Academic Information
            </div>
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
                    ? <span className="d-inline-flex align-items-center gap-1">0<Check size={13} /></span>
                    : String(profile.activeBacklogs)
                  : "—"
              } highlight={profile?.activeBacklogs === 0} />
            </div>
          </div>

          <div className="mp-card mp-card--full">
            <div className="mp-card-title d-flex align-items-center gap-2">
              <FileText size={16} />
              Documents
            </div>
            <div className="mp-docs">
              <DocRow label="Resume"     url={profile?.resumeUrl} />
              <DocRow label="Grade Card" url={profile?.gradeSheetUrl} />
            </div>
          </div>

          <div className="mp-card mp-card--full">
            <div className="mp-card-title d-flex align-items-center gap-2">
              <Trophy size={16} />
              Placement Information
            </div>
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
            <div className="mp-readonly-note d-flex align-items-center gap-2">
              <Lock size={13} />
              Profile is read-only. Contact the Placement Cell to update your information.
            </div>
          </div>

        </div>
      </div>
    </Layout>
  );
}

export default MyProfile;