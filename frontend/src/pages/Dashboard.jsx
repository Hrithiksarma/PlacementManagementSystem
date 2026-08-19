import { useEffect, useRef, useState, useCallback } from "react";
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Cell as BarCell,
  LineChart, Line, ReferenceLine,
} from "recharts";
import {
  Users, Building2, Briefcase, FileText, CheckCircle2, CalendarDays, XCircle,
  Trophy, BarChart3, TrendingDown, TrendingUp, Medal, Star, Pin, AlertTriangle,
  ChevronUp, ChevronDown, Hourglass, SlidersHorizontal, X,
} from "lucide-react";
import Layout from "../components/Layout";
import { getDashboardData } from "../services/dashboardService";
import "./Dashboard.css";

// ─── Constants ────────────────────────────────────────────────────────────────

const TIER_COLORS = {
  C:           "#7c3aed",
  B:           "#16a34a",
  A:           "#2563eb",
  "Unplaced":  "#374151",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

const STATUS_COLOR = {
  "Selected":              "#16a34a",
  "First Round":           "#7c3aed",
  "Interview Scheduled":   "#d97706",
  "Applied":               "#2563eb",
  "Rejected":              "#dc2626",
};

const PROGRAMS = ["All", "B.Tech", "M.Tech", "PhD"];

const EMPTY_FILTERS = { year: "", branch: "All", program: "All" };

// ─── Filter persistence (URL + sessionStorage) ────────────────────────────────

function readFiltersFromUrl() {
  const p = new URLSearchParams(window.location.search);
  const year    = p.get("year")    ?? "";
  const branch  = p.get("branch")  ?? "All";
  const program = p.get("program") ?? "All";
  if (year || branch !== "All" || program !== "All") return { year, branch, program };
  return null;
}

function loadFilters() {
  const fromUrl = readFiltersFromUrl();
  if (fromUrl) return fromUrl;
  try {
    const s = sessionStorage.getItem("dashFilters");
    return s ? JSON.parse(s) : EMPTY_FILTERS;
  } catch { return EMPTY_FILTERS; }
}

function persistFilters(f) {
  try { sessionStorage.setItem("dashFilters", JSON.stringify(f)); } catch {}
  const url = new URL(window.location.href);
  if (f.year)              url.searchParams.set("year",    f.year);
  else                     url.searchParams.delete("year");
  if (f.branch !== "All")  url.searchParams.set("branch",  f.branch);
  else                     url.searchParams.delete("branch");
  if (f.program !== "All") url.searchParams.set("program", f.program);
  else                     url.searchParams.delete("program");
  window.history.replaceState({}, "", url);
}

function apiParams(f) {
  const p = {};
  if (f.year && f.year !== "")   p.year    = f.year;
  if (f.branch  && f.branch  !== "All") p.branch  = f.branch;
  if (f.program && f.program !== "All") p.program = f.program;
  return p;
}

function isActive(f) {
  return (f.year && f.year !== "") ||
         (f.branch  && f.branch  !== "All") ||
         (f.program && f.program !== "All");
}

function filterLabel(f) {
  const parts = [];
  if (f.year)              parts.push(f.year);
  if (f.branch !== "All")  parts.push(f.branch);
  if (f.program !== "All") parts.push(f.program);
  return parts.join(" · ") || null;
}

// ─── Time ago ─────────────────────────────────────────────────────────────────

function timeAgo(dateStr) {
  if (!dateStr) return "—";
  const ms   = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(ms / 60000);
  const hrs  = Math.floor(ms / 3600000);
  const days = Math.floor(ms / 86400000);
  if (mins  < 1)  return "Just now";
  if (mins  < 60) return `${mins}m ago`;
  if (hrs   < 24) return `${hrs}h ago`;
  if (days  === 1) return "Yesterday";
  if (days  < 7)  return `${days} days ago`;
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short",
  });
}

function formatDate(dateStr) {
  if (!dateStr) return "—";
  return new Date(dateStr).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function fmt(val, decimals = 0) {
  if (val == null || val === 0) return "0";
  return typeof val === "number"
    ? decimals > 0 ? val.toFixed(decimals) : val.toLocaleString()
    : String(val);
}

// ─── Reusable small components ────────────────────────────────────────────────

function SectionHeader({ title, sub, badge }) {
  return (
    <div className="dash-section-header">
      <span className="dash-section-title">{title}</span>
      {sub   && <span className="dash-section-sub">{sub}</span>}
      {badge && <span className="dash-section-badge">{badge}</span>}
    </div>
  );
}

function TierBadge({ tier }) {
  const MAP = {
    C:           "tier-super-dream",
    B:           "tier-dream",
    A:           "tier-normal",
    "Unplaced":  "tier-unplaced",
  };
  return (
    <span className={`tier-badge ${MAP[tier] ?? "tier-unplaced"}`}>
      {tier ? (TIER_LABEL[tier] ?? tier) : "Unplaced"}
    </span>
  );
}

function EmptyState({ message }) {
  return (
    <div className="empty-chart-state">
      <BarChart3 className="empty-chart-icon" size={32} strokeWidth={1.6} />
      <p>{message ?? "No data available yet"}</p>
    </div>
  );
}

function Skeleton({ h = 80, r = 10 }) {
  return <div className="dash-skeleton" style={{ height: h, borderRadius: r }} />;
}

const PROGRAM_STATS_ROWS = [
  { label: "Total Registered",           key: "totalRegistered" },
  { label: "Opted for Higher Studies",   key: "optedForHigherStudies" },
  { label: "Total Jobs Offered",         key: "totalJobsOffered" },
  { label: "Average Salary",             key: "averageSalary",   isSalary: true },
  { label: "Median Salary",              key: "medianSalary",    isSalary: true },
  { label: "Total Students Selected",    key: "totalSelected" },
  { label: "Highest Salary",             key: "highestSalary",   isSalary: true },
  { label: "Percentage Placed",          key: "percentagePlaced", isPercent: true },
];

function ProgramStatsTable({ programStat }) {
  const categories = programStat.categories ?? [];
  return (
    <div>
      <div className="program-stats-title">{programStat.program} Placement</div>
      <div className="table-responsive">
        <table className="table table-bordered table-sm text-center align-middle mb-0 program-stats-table">
          <thead>
            <tr>
              <th className="text-start">Category</th>
              {categories.map((c) => <th key={c.branch}>{c.branch}</th>)}
            </tr>
          </thead>
          <tbody>
            {PROGRAM_STATS_ROWS.map((row) => (
              <tr key={row.key}>
                <td className="text-start fw-semibold">{row.label}</td>
                {categories.map((c) => (
                  <td key={c.branch}>
                    {row.isPercent ? `${fmt(c[row.key], 1)}%`
                      : row.isSalary ? (c[row.key] > 0 ? `${fmt(c[row.key], 1)} LPA` : "—")
                      : fmt(c[row.key])}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

// ─── KPI Cards ────────────────────────────────────────────────────────────────

function KpiCard({ icon, label, value, accent, sub }) {
  return (
    <div className="kpi-card" style={{ "--kpi-accent": accent }}>
      <div className="kpi-icon-wrap">{icon}</div>
      <div className="kpi-body">
        <div className="kpi-value">{value}</div>
        <div className="kpi-label">{label}</div>
        {sub && <div className="kpi-sub">{sub}</div>}
      </div>
    </div>
  );
}

function PlacementRateCard({ placed, total, percentage }) {
  return (
    <div className="kpi-card" style={{ "--kpi-accent": "#16a34a" }}>
      <div className="kpi-icon-wrap"><TrendingUp size={20} /></div>
      <div className="kpi-body">
        <div className="kpi-value" style={{ color: "#16a34a" }}>
          {fmt(percentage, 1)}%
        </div>
        <div className="kpi-label">Placement Rate</div>
        <div className="kpi-rate-breakdown">
          <span className="kpi-rate-placed">{placed} Placed</span>
          <span className="kpi-rate-sep"> / </span>
          <span className="kpi-rate-total">{total} Students</span>
        </div>
      </div>
    </div>
  );
}

function StatCard({ label, value, accent, icon, tooltip }) {
  return (
    <div className="stat-card" style={{ "--stat-accent": accent }}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
      {tooltip && (
        <div className="stat-tooltip-wrap">
          <span className="stat-tooltip-trigger">ⓘ</span>
          <div className="stat-tooltip-box">{tooltip}</div>
        </div>
      )}
    </div>
  );
}

// ─── Chart tooltips ───────────────────────────────────────────────────────────

function PieTooltip({ active, payload }) {
  if (!active || !payload?.length) return null;
  const { name, value } = payload[0];
  return (
    <div className="chart-tooltip">
      <strong>{name}</strong>: {value} student{value !== 1 ? "s" : ""}
    </div>
  );
}

function BarTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="chart-tooltip">
      <strong>{label}</strong><br />
      {d.placedStudents} / {d.totalStudents} placed ({payload[0].value}%)
    </div>
  );
}

function TrendTooltip({ active, payload, label }) {
  if (!active || !payload?.length) return null;
  const d = payload[0].payload;
  return (
    <div className="chart-tooltip">
      <strong>Batch {label}</strong><br />
      Placed: {d.placedStudents}<br />
      Total Students: {d.totalStudents}<br />
      <span style={{ color: "#93c5fd" }}>
        Placement Rate: {payload[0].value}%
      </span>
    </div>
  );
}

// ─── Filter Bar ───────────────────────────────────────────────────────────────

function FilterBar({ filters, onChange, onClear, availableYears, availableBranches }) {
  const active = isActive(filters);
  const sel    = (key) => (e) => onChange(key, e.target.value);

  return (
    <div className={`dash-filter-bar${active ? " dash-filter-bar--active" : ""}`}>
      <div className="filter-bar-label">
        <SlidersHorizontal size={13} /> Filters
        {active && <span className="filter-active-dot" />}
      </div>

      <div className="filter-controls">
        <div className="filter-group">
          <label className="filter-label">Academic Year</label>
          <select className="filter-select" value={filters.year} onChange={sel("year")}>
            <option value="">All Years</option>
            {availableYears.map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label className="filter-label">Branch</label>
          <select className="filter-select" value={filters.branch} onChange={sel("branch")}>
            <option value="All">All Branches</option>
            {availableBranches.map((b) => (
              <option key={b} value={b}>{b}</option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label className="filter-label">Program</label>
          <select className="filter-select" value={filters.program} onChange={sel("program")}>
            {PROGRAMS.map((p) => (
              <option key={p} value={p}>{p === "All" ? "All Programs" : p}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="filter-actions">
        {active && (
          <button className="filter-btn-clear d-inline-flex align-items-center gap-1" onClick={onClear}>
            Clear Filters
            <X size={12} />
          </button>
        )}
        {active && (
          <span className="filter-active-label">
            Filtered: {filterLabel(filters)}
          </span>
        )}
      </div>
    </div>
  );
}

// ─── Loading skeleton ─────────────────────────────────────────────────────────

function LoadingState() {
  return (
    <Layout>
      <div className="dash-container">
        <Skeleton h={54} />
        <div className="dash-row kpi-grid">
          {Array.from({ length: 6 }).map((_, i) => <Skeleton key={i} h={88} />)}
        </div>
        <div className="dash-row two-col"><Skeleton h={290} /><Skeleton h={290} /></div>
        <Skeleton h={200} />
        <Skeleton h={220} />
        <div className="dash-row two-col"><Skeleton h={280} /><Skeleton h={280} /></div>
        <Skeleton h={150} />
        <Skeleton h={70}  />
      </div>
    </Layout>
  );
}

// ─── Main Dashboard ───────────────────────────────────────────────────────────

function Dashboard() {
  const [data,     setData]     = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState(null);
  const [filters,  setFilters]  = useState(loadFilters);
  const [showAllRecruiters, setShowAllRecruiters] = useState(false);

  const debounce = useRef(null);
  const isFirst  = useRef(true);

  const fetchData = useCallback((f) => {
    setLoading(true);
    setError(null);
    getDashboardData(apiParams(f))
      .then((res) => { setData(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load dashboard data."); setLoading(false); });
  }, []);

  // Initial fetch
  useEffect(() => { fetchData(filters); }, []);

  // Auto-refresh on filter change (debounced, skip first render)
  useEffect(() => {
    if (isFirst.current) { isFirst.current = false; return; }
    clearTimeout(debounce.current);
    debounce.current = setTimeout(() => {
      persistFilters(filters);
      setShowAllRecruiters(false);
      fetchData(filters);
    }, 380);
    return () => clearTimeout(debounce.current);
  }, [filters.year, filters.branch, filters.program]);

  const handleFilterChange = (key, value) =>
    setFilters((f) => ({ ...f, [key]: value }));

  const handleClearFilters = () => setFilters(EMPTY_FILTERS);

  if (loading) return <LoadingState />;

  if (error) {
    return (
      <Layout>
        <div className="alert alert-danger m-3">{error}</div>
      </Layout>
    );
  }

  // ── Derived ──────────────────────────────────────────────────────────────

  const tierPieData = [
    { name: "Tier C",   key: "C", value: data.tierCCount },
    { name: "Tier B",   key: "B", value: data.tierBCount },
    { name: "Tier A",   key: "A", value: data.tierACount },
    { name: "Unplaced", key: "Unplaced", value: data.unplacedCount },
  ].filter((d) => d.value > 0);

  const branchBarData = (data.branchWisePlacement ?? []).map((b) => ({
    branch:              b.branch,
    placementPercentage: b.placementPercentage,
    placedStudents:      b.placedStudents,
    totalStudents:       b.totalStudents,
  }));

  const trendData = (data.placementTrends ?? []).map((t) => ({
    year:                t.year,
    placementPercentage: t.placementPercentage,
    placedStudents:      t.placedStudents,
    totalStudents:       t.totalStudents,
  }));

  const displayedRecruiters = showAllRecruiters
    ? (data.topRecruiters ?? [])
    : (data.topRecruiters ?? []).slice(0, 5);

  const activeBadge = isActive(filters) ? filterLabel(filters) : null;
  const topBranch   = branchBarData[0];
  const topRecruiter = data.topRecruiters?.[0];

  return (
    <Layout>
      <div className="dash-container">

        {/* ── Filter Bar (auto-refresh) ────────────────────────────────── */}
        <FilterBar
          filters={filters}
          onChange={handleFilterChange}
          onClear={handleClearFilters}
          availableYears={data.availableYears ?? []}
          availableBranches={data.availableBranches ?? []}
        />

        {/* ── Row 1 · KPI Grid ─────────────────────────────────────────── */}
        <div className="dash-row kpi-grid">
          <KpiCard icon={<Users size={20} />} label="Total Students"
            value={fmt(data.totalStudents)} accent="#2563eb" />
          <KpiCard icon={<Building2 size={20} />} label="Companies"
            value={fmt(data.totalCompanies)} accent="#7c3aed" />
          <KpiCard icon={<Briefcase size={20} />} label="Drives"
            value={fmt(data.totalDrives)} accent="#0891b2" />
          <KpiCard icon={<FileText size={20} />} label="Applications"
            value={fmt(data.totalApplications)} accent="#ea580c" />
          <KpiCard icon={<CheckCircle2 size={20} />} label="Placed Students"
            value={fmt(data.totalPlacedStudents)} accent="#16a34a" />
          <PlacementRateCard
            placed={data.totalPlacedStudents}
            total={data.totalStudents}
            percentage={data.placementPercentage}
          />
        </div>

        {/* ── Row 2 · Tier Pie + Drive & Package Stats ─────────────────── */}
        <div className="dash-row two-col">

          <div className="dash-card">
            <SectionHeader
              title="Placement Tier Distribution"
              sub={`${data.totalStudents} students`}
              badge={activeBadge}
            />
            {tierPieData.length === 0 ? (
              <EmptyState message="No tier data assigned yet" />
            ) : (
              <ResponsiveContainer width="100%" height={250}>
                <PieChart>
                  <Pie data={tierPieData} cx="50%" cy="48%"
                    innerRadius={58} outerRadius={95} paddingAngle={3} dataKey="value">
                    {tierPieData.map((e) => (
                      <Cell key={e.name} fill={TIER_COLORS[e.key] ?? "#94a3b8"} />
                    ))}
                  </Pie>
                  <Tooltip content={<PieTooltip />} />
                  <Legend iconType="circle" iconSize={9}
                    formatter={(value, entry) => (
                      <span style={{ fontSize: "0.76rem", color: "#475569" }}>
                        {value} <strong style={{ color: "#1e293b" }}>({entry.payload.value})</strong>
                      </span>
                    )}
                  />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>

          <div className="dash-card">
            <SectionHeader title="Drive Statistics" sub={`${data.totalDrives} total`} />
            <div className="stat-grid-3">
              <StatCard label="Upcoming"  value={data.upcomingDrives}  accent="#2563eb" icon={<CalendarDays size={18} />} />
              <StatCard label="Completed" value={data.completedDrives} accent="#16a34a" icon={<CheckCircle2 size={18} />} />
              <StatCard label="Cancelled" value={data.cancelledDrives} accent="#dc2626" icon={<XCircle size={18} />} />
            </div>
            <div className="dash-divider" />
            <SectionHeader title="Package Analytics" sub="All drives" />
            <div className="stat-grid-3">
              <StatCard label="Highest"
                value={data.highestPackage > 0 ? `${data.highestPackage} LPA` : "—"}
                accent="#7c3aed" icon={<Trophy size={18} />} />
              <StatCard label="Average"
                value={data.averagePackage > 0 ? `${fmt(data.averagePackage, 1)} LPA` : "—"}
                accent="#0891b2" icon={<BarChart3 size={18} />} />
              <StatCard label="Median"
                value={data.medianPackage > 0 ? `${fmt(data.medianPackage, 1)} LPA` : "—"}
                accent="#ea580c" icon={<TrendingDown size={18} />} />
            </div>
          </div>
        </div>

        {/* ── Placement Trend ───────────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Placement Trend by Batch Year"
            sub="Global · Placed ÷ Total Students per year"
          />
          {trendData.length < 2 ? (
            <EmptyState message={
              trendData.length === 1
                ? "Only one batch year found — add more data for a trend"
                : "No batch year data found"
            } />
          ) : (
            <ResponsiveContainer width="100%" height={200}>
              <LineChart data={trendData} margin={{ top: 8, right: 36, left: 0, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
                <XAxis dataKey="year" tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false} tickLine={false} />
                <YAxis domain={[0, 100]} tickFormatter={(v) => `${v}%`}
                  tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false} tickLine={false} width={38} />
                <Tooltip content={<TrendTooltip />} />
                <ReferenceLine y={50} stroke="#e2e8f0" strokeDasharray="4 4" />
                <Line type="monotone" dataKey="placementPercentage"
                  stroke="#2563eb" strokeWidth={2.5}
                  dot={{ r: 4, fill: "#2563eb", stroke: "#fff", strokeWidth: 2 }}
                  activeDot={{ r: 6, fill: "#2563eb" }} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* ── Branch-wise Bar Chart ─────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Branch-wise Placement Performance"
            sub="Sorted highest → lowest"
            badge={activeBadge}
          />
          {branchBarData.length === 0 ? (
            <EmptyState message="No branch data for the selected filters" />
          ) : (
            <ResponsiveContainer width="100%"
              height={Math.max(160, branchBarData.length * 46)}>
              <BarChart data={branchBarData} layout="vertical"
                margin={{ top: 4, right: 64, left: 16, bottom: 4 }}>
                <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#f1f5f9" />
                <XAxis type="number" domain={[0, 100]}
                  tickFormatter={(v) => `${v}%`}
                  tick={{ fontSize: 11, fill: "#94a3b8" }}
                  axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="branch" width={94}
                  tick={{ fontSize: 12, fill: "#475569", fontWeight: 600 }}
                  axisLine={false} tickLine={false} />
                <Tooltip content={<BarTooltip />} />
                <Bar dataKey="placementPercentage" radius={[0, 4, 4, 0]}
                  label={{
                    position: "right",
                    formatter: (v) => `${v}%`,
                    fontSize: 11, fill: "#475569", fontWeight: 600,
                  }}>
                  {branchBarData.map((_, i) => (
                    <BarCell
                      key={i}
                      fill={i === 0 ? "#1d4ed8" : i === 1 ? "#2563eb" : "#3b82f6"}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* ── Placement Statistics by Program (B.Tech / M.Tech × branch) ──── */}
        {(data.programStats?.length ?? 0) > 0 && (
          <div className="dash-card">
            <SectionHeader
              title="Placement Statistics by Program"
              sub="Category-wise breakdown, current selection"
              badge={activeBadge}
            />
            <div className="d-flex flex-column gap-4">
              {data.programStats.map((ps) => (
                <ProgramStatsTable key={ps.program} programStat={ps} />
              ))}
            </div>
          </div>
        )}

        {/* ── Companies Placed ─────────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Companies Placed"
            sub={(data.placedCompanies?.length ?? 0) > 0
              ? `${data.placedCompanies.length} compan${data.placedCompanies.length !== 1 ? "ies" : "y"}`
              : undefined}
            badge={activeBadge}
          />
          {!(data.placedCompanies?.length) ? (
            <EmptyState message="No companies have placed students yet" />
          ) : (
            <div className="placed-companies-list">
              {data.placedCompanies.map((name) => (
                <span key={name} className="placed-company-chip">{name}</span>
              ))}
            </div>
          )}
        </div>

        {/* ── Top Recruiters + Recent Activities ───────────────────────── */}
        <div className="dash-row two-col">

          {/* Top Recruiters */}
          <div className="dash-card">
            <SectionHeader
              title="Top Recruiting Companies"
              sub={(data.topRecruiters?.length ?? 0) > 0
                ? `${data.topRecruiters.length} recruiter${data.topRecruiters.length !== 1 ? "s" : ""} · by hires`
                : undefined}
              badge={activeBadge}
            />

            {!(data.topRecruiters?.length) ? (
              <EmptyState message="No placement records available yet" />
            ) : (
              <>
                <div className="recruiter-table">
                  <div className="recruiter-header">
                    <span className="rh-rank">#</span>
                    <span className="rh-name">Company</span>
                    <span className="rh-tier">Tier</span>
                    <span className="rh-count">Hires</span>
                  </div>
                  {displayedRecruiters.map((r, i) => (
                    <div key={r.companyName}
                      className={`recruiter-row${i === 0 ? " recruiter-row--top" : ""}`}>
                      <span className="recruiter-rank">
                        {i === 0 ? <Medal size={16} color="#eab308" />
                          : i === 1 ? <Medal size={16} color="#94a3b8" />
                          : i === 2 ? <Medal size={16} color="#b45309" />
                          : i + 1}
                      </span>
                      <span className="recruiter-name">{r.companyName}</span>
                      <span className="recruiter-tier"><TierBadge tier={r.tier} /></span>
                      <span className="recruiter-count">{r.studentsHired}</span>
                    </div>
                  ))}
                </div>

                <div className="recruiter-footer">
                  <span className="recruiter-total-label">
                    Total Recruiters: <strong>{data.topRecruiters.length}</strong>
                  </span>
                  {(data.topRecruiters?.length ?? 0) > 5 && (
                    <button className="view-all-btn d-inline-flex align-items-center gap-1"
                      onClick={() => setShowAllRecruiters((v) => !v)}>
                      {showAllRecruiters ? "Show Less" : `View All ${data.topRecruiters.length}`}
                      {showAllRecruiters ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
                    </button>
                  )}
                </div>
              </>
            )}
          </div>

          {/* Recent Activities */}
          <div className="dash-card">
            <SectionHeader
              title="Recent Placement Activities"
              sub="Latest 10 events"
              badge={activeBadge}
            />
            {!(data.recentActivities?.length) ? (
              <EmptyState message="No application activity yet" />
            ) : (
              <div className="activity-feed">
                {data.recentActivities.map((a, i) => (
                  <div key={i} className="activity-item">
                    <span
                      className="activity-status-icon"
                      style={{
                        display: "inline-block", width: 9, height: 9, borderRadius: "50%",
                        background: STATUS_COLOR[a.status] ?? "#94a3b8",
                      }}
                    />
                    <div className="activity-body">
                      <span className="activity-student">{a.studentName}</span>
                      <span className="activity-connector">
                        {a.status === "Selected"            ? " selected at "      :
                         a.status === "First Round"         ? " cleared first round at " :
                         a.status === "Interview Scheduled" ? " interview at "     :
                         a.status === "Rejected"            ? " rejected at "      :
                                                              " applied at "}
                      </span>
                      <span className="activity-company">{a.companyName}</span>
                    </div>
                    <div className="activity-meta">
                      <span className="activity-badge"
                        style={{ background: STATUS_COLOR[a.status] ?? "#94a3b8" }}>
                        {a.status}
                      </span>
                      <span className="activity-date"
                        title={formatDate(a.applicationDate)}>
                        {timeAgo(a.applicationDate)}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* ── Placement Insights ────────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Quick Placement Insights"
            sub="Key metrics at a glance"
            badge={activeBadge}
          />
          <div className="insights-grid">
            <InsightItem
              icon={<Trophy size={18} />}
              label="Highest Placement Branch"
              value={topBranch
                ? `${topBranch.branch} (${topBranch.placementPercentage}%)`
                : "—"}
              accent="#2563eb"
            />
            <InsightItem
              icon={<Medal size={18} color="#eab308" />}
              label="Top Recruiter"
              value={topRecruiter
                ? `${topRecruiter.companyName} (${topRecruiter.studentsHired} hires)`
                : "—"}
              accent="#7c3aed"
            />
            <InsightItem
              icon={<Pin size={18} />}
              label="Most Applied Company"
              value={data.mostAppliedCompanyName
                ? `${data.mostAppliedCompanyName} (${data.mostAppliedCompanyCount} applications)`
                : "—"}
              accent="#ea580c"
            />
            <InsightItem
              icon={<TrendingUp size={18} />}
              label="Placement Rate"
              value={`${fmt(data.placementPercentage, 1)}%`}
              accent="#16a34a"
            />
            <InsightItem
              icon={<Hourglass size={18} />}
              label="Students Remaining"
              value={fmt(data.totalStudents - data.totalPlacedStudents)}
              accent="#d97706"
            />
          </div>
        </div>

        {/* ── Eligibility Snapshot ──────────────────────────────────────── */}
        <div className="dash-card">
          <SectionHeader
            title="Student Eligibility Snapshot"
            sub="Tier C ≥ 8.5 CGPA · Tier B ≥ 7.0 CGPA · hover cards for details"
            badge={activeBadge}
          />
          <div className="stat-grid-4">
            <StatCard
              label="No Active Backlogs"
              value={data.noBacklogStudents}
              accent="#16a34a"
              icon={<CheckCircle2 size={18} />}
              tooltip="Students whose current active backlog count is zero"
            />
            <StatCard
              label="Tier B Eligible (CGPA ≥ 7.0)"
              value={data.dreamEligibleStudents}
              accent="#16a34a"
              icon={<Star size={18} />}
              tooltip="Students with CGPA ≥ 7.0 and no active backlogs"
            />
            <StatCard
              label="Tier C Eligible (CGPA ≥ 8.5)"
              value={data.superDreamEligibleStudents}
              accent="#7c3aed"
              icon={<Trophy size={18} />}
              tooltip="Students with CGPA ≥ 8.5 and no active backlogs"
            />
            <StatCard
              label="Students with Backlogs"
              value={data.studentsWithBacklogs}
              accent="#dc2626"
              icon={<AlertTriangle size={18} />}
              tooltip="Students having one or more active backlogs"
            />
          </div>
        </div>

        {/* ── Footer Summary ────────────────────────────────────────────── */}
        <div className="dash-footer-card">
          <span className="footer-label">Data Summary</span>
          <div className="footer-stats">
            <FooterStat label="Students"     value={data.totalStudents} />
            <FooterStat label="Companies"    value={data.totalCompanies} />
            <FooterStat label="Drives"       value={data.totalDrives} />
            <FooterStat label="Applications" value={data.totalApplications} />
            <FooterStat label="Placed"       value={data.totalPlacedStudents} />
          </div>
          <span className="footer-updated">
            Last updated: {data.lastUpdated ?? "—"}
          </span>
        </div>

      </div>
    </Layout>
  );
}

// ─── InsightItem ──────────────────────────────────────────────────────────────

function InsightItem({ icon, label, value, accent }) {
  return (
    <div className="insight-item" style={{ "--insight-accent": accent }}>
      <span className="insight-icon">{icon}</span>
      <div className="insight-body">
        <div className="insight-label">{label}</div>
        <div className="insight-value">{value}</div>
      </div>
    </div>
  );
}

// ─── FooterStat ───────────────────────────────────────────────────────────────

function FooterStat({ label, value }) {
  return (
    <div className="footer-stat">
      <span className="footer-stat-value">{value}</span>
      <span className="footer-stat-label">{label}</span>
    </div>
  );
}

export default Dashboard;
