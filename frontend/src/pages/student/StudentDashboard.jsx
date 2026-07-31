import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Mail, Target, FileText, Star, CalendarDays, Trophy, ClipboardList, User,
  PartyPopper, Sparkles, ArrowRight,
} from "lucide-react";
import Layout from "../../components/Layout";
import {
  getStudentProfile,
  getEligibleDrives,
  getStudentApplications,
  getRecentAchievements,
} from "../../services/studentPortalService";
import "./StudentDashboard.css";

const PLACEMENT_COLOR = {
  C:           "#7c3aed",
  B:           "#16a34a",
  A:           "#2563eb",
  "Unplaced":  "#475569",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };


function StatCard({ icon, label, value, accent, to }) {
  const inner = (
    <div className="sd-stat-card" style={{ "--sd-accent": accent }}>
      <div className="sd-stat-icon">{icon}</div>
      <div className="sd-stat-value">{value ?? "—"}</div>
      <div className="sd-stat-label">{label}</div>
    </div>
  );
  return to ? <Link to={to} className="sd-stat-link">{inner}</Link> : inner;
}

function AppStatusPill({ status }) {
  const COLOR = {
    Applied:               "#2563eb",
    "First Round":         "#7c3aed",
    "Interview Scheduled": "#d97706",
    Selected:              "#16a34a",
    Rejected:              "#dc2626",
    Withdrawn:             "#94a3b8",
  };
  return (
    <span className="sd-app-status" style={{ background: COLOR[status] ?? "#94a3b8" }}>
      {status}
    </span>
  );
}

function Skeleton({ h = 60 }) {
  return <div className="sd-skeleton" style={{ height: h }} />;
}

function StudentDashboard() {
  const [profile,      setProfile]      = useState(null);
  const [eligible,     setEligible]     = useState([]);
  const [apps,         setApps]         = useState([]);
  const [achievements, setAchievements] = useState([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState(null);

  useEffect(() => {
    Promise.all([
      getStudentProfile(),
      getEligibleDrives(),
      getStudentApplications(),
      getRecentAchievements(),
    ])
      .then(([p, e, a, ach]) => {
        setProfile(p.data);
        setEligible(e.data);
        setApps(a.data);
        setAchievements(ach.data);
        setLoading(false);
      })
      .catch(() => {
        setError("Failed to load your dashboard. Please try again.");
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="sd-container">
          <Skeleton h={130} />
          <div className="sd-stat-grid">
            {[1,2,3,4,5].map((i) => <Skeleton key={i} />)}
          </div>
          <Skeleton h={120} />
          <Skeleton h={200} />
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="alert alert-danger m-3">{error}</div>
      </Layout>
    );
  }

  // Only count drives the student is genuinely eligible for
  const eligibleCount    = eligible.filter((d) => d.eligible !== false).length;
  const firstRoundCount  = apps.filter((a) => a.status === "First Round").length;
  const interviewCount   = apps.filter((a) => a.status === "Interview Scheduled").length;
  const offersCount      = apps.filter((a) => a.status === "Selected").length;

  const recentApps = apps.slice(0, 5);

  // Open drives the student is eligible for and hasn't applied to yet
  const openDrives = eligible
    .filter((d) => d.eligible !== false && !d.alreadyApplied && d.driveStatus !== "Completed")
    .slice(0, 4);

  return (
    <Layout>
      <div className="sd-container">

        {/* ── Welcome Banner ─────────────────────────────────────────────── */}
        <div className="sd-welcome-card">
          <div className="sd-avatar">
            {profile?.name?.charAt(0)?.toUpperCase() ?? "S"}
          </div>
          <div className="sd-welcome-body">
            <div className="sd-greeting">Welcome back,</div>
            <div className="sd-name">{profile?.name ?? "Student"}</div>
            <div className="sd-meta-row">
              {profile?.branch    && <span className="sd-meta-chip">{profile.branch}</span>}
              {profile?.program   && <span className="sd-meta-chip">{profile.program}</span>}
              {profile?.batchYear && <span className="sd-meta-chip">Batch {profile.batchYear}</span>}
              {profile?.cgpa != null && (
                <span className="sd-meta-chip">CGPA {profile.cgpa}</span>
              )}
            </div>
          </div>
          <div className="sd-welcome-right">
            {(() => {
              const placed = profile?.placementTier && profile.placementTier !== "Unplaced";
              const hasPending = !placed && apps.some((a) => a.status === "Selected");
              const label  = placed
                ? `Placed (${TIER_LABEL[profile.placementTier] ?? profile.placementTier})`
                : hasPending ? <><Mail size={12} /> Offer Pending</>
                : "Not Placed";
              const bg = placed
                ? (PLACEMENT_COLOR[profile.placementTier] ?? "#475569")
                : hasPending ? "#d97706"
                : "#475569";
              return (
                <span className="sd-tier-badge d-inline-flex align-items-center gap-1" style={{ background: bg }}>
                  {label}
                </span>
              );
            })()}
          </div>
        </div>

        {/* ── Stat Cards ─────────────────────────────────────────────────── */}
        <div className="sd-stat-grid">
          <StatCard
            icon={<Target size={20} />} label="Eligible Drives"
            value={eligibleCount}
            accent="#2563eb"
            to="/student/eligible-drives"
          />
          <StatCard
            icon={<FileText size={20} />} label="Applied"
            value={apps.length}
            accent="#7c3aed"
            to="/student/applications"
          />
          <StatCard
            icon={<Star size={20} />} label="First Round"
            value={firstRoundCount}
            accent="#d97706"
            to="/student/applications"
          />
          <StatCard
            icon={<CalendarDays size={20} />} label="Interviews"
            value={interviewCount}
            accent="#ea580c"
            to="/student/applications"
          />
          <StatCard
            icon={<Trophy size={20} />} label="Offers"
            value={offersCount}
            accent="#16a34a"
            to="/student/placement-status"
          />
        </div>

        {/* ── Quick Actions ───────────────────────────────────────────────── */}
        <div className="sd-section-card">
          <div className="sd-section-head">Quick Actions</div>
          <div className="sd-quick-links">
            <Link to="/student/eligible-drives"  className="sd-quick-btn sd-qb-blue d-inline-flex align-items-center gap-2">
              <Target size={15} />Browse Drives
            </Link>
            <Link to="/student/applications"     className="sd-quick-btn sd-qb-purple d-inline-flex align-items-center gap-2">
              <ClipboardList size={15} />My Applications
            </Link>
            <Link to="/student/profile"          className="sd-quick-btn sd-qb-slate d-inline-flex align-items-center gap-2">
              <User size={15} />View Profile
            </Link>
            <Link to="/student/placement-status" className="sd-quick-btn sd-qb-green d-inline-flex align-items-center gap-2">
              <Trophy size={15} />Placement Status
            </Link>
          </div>
        </div>

        {/* ── Recent Placement Achievements (public — Selected only) ──────── */}
        {achievements.length > 0 && (
          <div className="sd-section-card">
            <div className="sd-section-head d-flex align-items-center gap-2">
              <PartyPopper size={16} />
              Recent Placement Achievements
            </div>
            <div className="sd-achievement-list">
              {achievements.map((a, i) => (
                <div key={i} className="sd-achievement-row">
                  <div className="sd-achievement-icon"><Sparkles size={16} color="#7c3aed" /></div>
                  <div className="sd-achievement-body">
                    <span className="sd-achievement-name">{a.studentName}</span>
                    <span className="sd-achievement-text"> selected at </span>
                    <span className="sd-achievement-company">{a.companyName ?? "—"}</span>
                    {a.roleOffered && (
                      <span className="sd-achievement-role"> · {a.roleOffered}</span>
                    )}
                  </div>
                  {a.packageLpa && (
                    <span className="sd-achievement-pkg">{a.packageLpa} LPA</span>
                  )}
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ── My Recent Applications (own data only) ──────────────────────── */}
        {recentApps.length > 0 && (
          <div className="sd-section-card">
            <div className="sd-section-head">
              My Recent Applications
              <Link to="/student/applications" className="sd-section-link">View All</Link>
            </div>
            <div className="sd-app-list">
              {recentApps.map((a) => (
                <div key={a.applicationId} className="sd-app-row">
                  <div className="sd-app-company">{a.companyName ?? "—"}</div>
                  <div className="sd-app-role">{a.roleOffered ?? "—"}</div>
                  <AppStatusPill status={a.status} />
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ── Open Drives You Can Apply To ────────────────────────────────── */}
        {openDrives.length > 0 && (
          <div className="sd-section-card">
            <div className="sd-section-head">
              Open Drives You Can Apply To
              <Link to="/student/eligible-drives" className="sd-section-link">View All</Link>
            </div>
            <div className="sd-drive-list">
              {openDrives.map((d) => (
                <div key={d.driveId} className="sd-drive-row">
                  <div className="sd-drive-company">{d.companyName ?? "—"}</div>
                  <div className="sd-drive-role">{d.roleOffered}</div>
                  {d.packageLpa && (
                    <span className="sd-drive-pkg">{d.packageLpa} LPA</span>
                  )}
                  <Link to="/student/eligible-drives" className="sd-apply-link d-inline-flex align-items-center gap-1">
                    Apply<ArrowRight size={12} />
                  </Link>
                </div>
              ))}
            </div>
          </div>
        )}

      </div>
    </Layout>
  );
}

export default StudentDashboard;
