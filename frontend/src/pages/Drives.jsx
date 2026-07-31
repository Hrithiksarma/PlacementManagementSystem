import { useEffect, useState, Fragment } from "react";
import { Eye, EyeOff, Lock } from "lucide-react";
import Layout from "../components/Layout";
import { getAllDrives, addDrive, updateDrive, updateDriveStatus, deleteDrive } from "../services/driveService";
import { getAllCompanies } from "../services/companyService";
import { isAdmin } from "../services/authService";
import "./Drives.css";

const TIERS    = ["All", "A", "B", "C"];
const STATUSES = ["Upcoming", "Active", "Completed", "Cancelled"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t === "All" ? "All Tiers" : t ? `Tier ${t}` : t);

const emptyForm = {
  companyId:           "",
  roleOffered:         "",
  packageLpa:          "",
  minCgpa:             "",
  maxBacklogs:         "",
  pptDate:             "",
  resumeSelectionDate: "",
  driveDate:           "",
  finalSelectionDate:  "",
  status:              "Upcoming",
};

const STATUS_BADGE = {
  Upcoming:  "primary",
  Active:    "success",
  Completed: "secondary",
  Cancelled: "danger",
};

function Drives() {
  const [companies,   setCompanies]   = useState([]);
  const [allDrives,   setAllDrives]   = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [filters,     setFilters]     = useState({ companyId: "All", tier: "All" });
  const [showForm,    setShowForm]    = useState(false);
  const [editingId,   setEditingId]   = useState(null);
  const [form,        setForm]        = useState(emptyForm);
  const [expandedDatesId, setExpandedDatesId] = useState(null);

  const admin = isAdmin();

  // A Placement Officer can delete only non-completed drives
  const canDeleteDrive = (drive) =>
    admin || drive.status?.toLowerCase() !== "completed";

  useEffect(() => {
    getAllCompanies()
      .then((res) => setCompanies(res.data))
      .catch((err) => console.error("Error loading companies:", err));
  }, []);

  const handleSearch = async () => {
    try {
      const res = await getAllDrives();
      setAllDrives(res.data);
      setHasSearched(true);
    } catch (err) {
      console.error("Error loading drives:", err);
    }
  };

  const buildPayload = () => ({
    company:             form.companyId ? { companyId: parseInt(form.companyId) } : null,
    roleOffered:         form.roleOffered,
    packageLpa:          form.packageLpa  !== "" ? parseFloat(form.packageLpa)  : null,
    minCgpa:             form.minCgpa     !== "" ? parseFloat(form.minCgpa)     : null,
    maxBacklogs:         form.maxBacklogs !== "" ? parseInt(form.maxBacklogs)   : null,
    pptDate:             form.pptDate || null,
    resumeSelectionDate: form.resumeSelectionDate || null,
    driveDate:           form.driveDate || null,
    finalSelectionDate:  form.finalSelectionDate || null,
    status:              form.status || "Upcoming",
  });

  const handleSaveDrive = async () => {
    try {
      if (editingId) {
        await updateDrive(editingId, buildPayload());
        alert("Drive Updated Successfully");
      } else {
        await addDrive(buildPayload());
        alert("Drive Added Successfully");
      }
      if (hasSearched) handleSearch();
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
    } catch (err) {
      console.error(err);
    }
  };

  const handleEdit = (drive) => {
    setForm({
      companyId:           drive.company?.companyId ?? "",
      roleOffered:         drive.roleOffered ?? "",
      packageLpa:          drive.packageLpa  ?? "",
      minCgpa:             drive.minCgpa     ?? "",
      maxBacklogs:         drive.maxBacklogs ?? "",
      pptDate:             drive.pptDate             ?? "",
      resumeSelectionDate: drive.resumeSelectionDate  ?? "",
      driveDate:           drive.driveDate   ?? "",
      finalSelectionDate:  drive.finalSelectionDate  ?? "",
      status:              drive.status      ?? "Upcoming",
    });
    setEditingId(drive.driveId);
    setShowForm(true);
  };

  const handleDelete = async (drive) => {
    if (!canDeleteDrive(drive)) {
      alert("Placement Officers cannot delete completed drives. Contact an Admin.");
      return;
    }
    if (!window.confirm("Delete this drive?")) return;
    try {
      await deleteDrive(drive.driveId);
      alert("Drive Deleted Successfully");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
      const data = err.response?.data;
      const message = typeof data === "string" ? data : data?.message;
      alert(message ?? "Failed to delete drive");
    }
  };

  const handleStatusChange = async (drive, newStatus) => {
    if (newStatus === drive.status) return;
    const previous = allDrives;
    setAllDrives(allDrives.map((d) =>
      d.driveId === drive.driveId ? { ...d, status: newStatus } : d
    ));
    try {
      await updateDriveStatus(drive.driveId, newStatus);
    } catch (err) {
      console.error(err);
      setAllDrives(previous);
      const data = err.response?.data;
      const message = typeof data === "string" ? data : data?.message;
      alert(message ?? "Failed to update drive status");
    }
  };

  const field = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  });

  const displayedDrives = allDrives
    .filter((d) =>
      filters.companyId === "All" ||
      String(d.company?.companyId) === filters.companyId
    )
    .filter((d) =>
      filters.tier === "All" || d.company?.tier === filters.tier
    );

  const companyLabel =
    filters.companyId === "All"
      ? "All Companies"
      : companies.find((c) => String(c.companyId) === filters.companyId)
          ?.companyName ?? "All Companies";
  const tierLabel = tierLabelFor(filters.tier);

  const formatDate = (dateStr) => {
    if (!dateStr) return "—";
    return new Date(dateStr).toLocaleDateString("en-IN", {
      day: "2-digit", month: "short",
    });
  };

  return (
    <Layout>
      <div className="d-flex justify-content-between mb-3">
        <h2>Drives</h2>
        <button
          className="btn btn-primary"
          onClick={() => {
            setShowForm(!showForm);
            if (showForm) { setEditingId(null); setForm(emptyForm); }
          }}
        >
          {showForm ? "Cancel" : "Add Drive"}
        </button>
      </div>

      {/* Filter Panel */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-4">
              <label className="form-label fw-semibold">Company</label>
              <select
                className="form-select"
                value={filters.companyId}
                onChange={(e) => setFilters({ ...filters, companyId: e.target.value })}
              >
                <option value="All">All</option>
                {companies.map((c) => (
                  <option key={c.companyId} value={String(c.companyId)}>
                    {c.companyName}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label fw-semibold">Tier</label>
              <select
                className="form-select"
                value={filters.tier}
                onChange={(e) => setFilters({ ...filters, tier: e.target.value })}
              >
                {TIERS.map((t) => (
                  <option key={t} value={t}>{tierLabelFor(t)}</option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <button className="btn btn-primary w-100" onClick={handleSearch}>
                Load Drives
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Add / Edit Form */}
      {showForm && (
        <div className="card mb-4">
          <div className="card-body">
            <div className="row g-2">
              <div className="col-md-6">
                <label className="form-label">Company</label>
                <select className="form-select" {...field("companyId")}>
                  <option value="">Select company</option>
                  {companies.map((c) => (
                    <option key={c.companyId} value={c.companyId}>
                      {c.companyName}
                    </option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Role</label>
                <input
                  className="form-control"
                  placeholder="e.g. SWE Intern"
                  {...field("roleOffered")}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Package (LPA)</label>
                <input
                  className="form-control"
                  type="number"
                  step="0.5"
                  min="0"
                  placeholder="e.g. 12.5"
                  {...field("packageLpa")}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Min CGPA</label>
                <input
                  className="form-control"
                  type="number"
                  step="0.1"
                  min="0"
                  max="10"
                  placeholder="e.g. 7.5"
                  {...field("minCgpa")}
                />
              </div>
              <div className="col-md-4">
                <label className="form-label">Max Backlogs</label>
                <input
                  className="form-control"
                  type="number"
                  min="0"
                  placeholder="e.g. 0"
                  {...field("maxBacklogs")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Pre-Placement Talk Date</label>
                <input
                  className="form-control"
                  type="date"
                  {...field("pptDate")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Resume Selection Date</label>
                <input
                  className="form-control"
                  type="date"
                  {...field("resumeSelectionDate")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Exam Date</label>
                <input
                  className="form-control"
                  type="date"
                  {...field("driveDate")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Final Selection Date</label>
                <input
                  className="form-control"
                  type="date"
                  {...field("finalSelectionDate")}
                />
              </div>
              <div className="col-md-6">
                <label className="form-label">Status</label>
                <select className="form-select" {...field("status")}>
                  {STATUSES.map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
            </div>
            <button className="btn btn-success mt-3" onClick={handleSaveDrive}>
              {editingId ? "Update Drive" : "Save Drive"}
            </button>
          </div>
        </div>
      )}

      {/* Results */}
      {hasSearched && (
        <>
          <p className="text-muted mb-2">
            Showing <strong>{displayedDrives.length}</strong> drive(s) — {companyLabel} • {tierLabel}
            {!admin && (
              <span className="badge bg-warning text-dark ms-2" style={{ fontSize: "0.7rem" }}>
                Completed drives locked
              </span>
            )}
          </p>

          {displayedDrives.length === 0 ? (
            <div className="alert alert-warning">
              No drives found for selected filters.
            </div>
          ) : (
            <table className="table table-striped table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>ID</th>
                  <th>Company</th>
                  <th>Tier</th>
                  <th>Role</th>
                  <th>Package (LPA)</th>
                  <th>Min CGPA</th>
                  <th>Max Backlogs</th>
                  <th>Dates</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {displayedDrives.map((drive) => {
                  const isExpanded = expandedDatesId === drive.driveId;
                  return (
                  <Fragment key={drive.driveId}>
                    <tr>
                      <td>{drive.driveId}</td>
                      <td>{drive.company?.companyName ?? "—"}</td>
                      <td>{drive.company?.tier ? tierLabelFor(drive.company.tier) : "—"}</td>
                      <td>{drive.roleOffered}</td>
                      <td>{drive.packageLpa ?? "—"}</td>
                      <td>{drive.minCgpa ?? "—"}</td>
                      <td>{drive.maxBacklogs ?? "—"}</td>
                      <td>
                        <button
                          className="btn btn-sm btn-outline-primary d-inline-flex align-items-center gap-1"
                          onClick={() => setExpandedDatesId(isExpanded ? null : drive.driveId)}
                        >
                          {isExpanded ? <EyeOff size={13} /> : <Eye size={13} />}
                          {isExpanded ? "Hide" : "View"}
                        </button>
                      </td>
                      <td>
                        <select
                          className={`form-select form-select-sm border-${STATUS_BADGE[drive.status] ?? "secondary"} text-${STATUS_BADGE[drive.status] ?? "secondary"} fw-semibold`}
                          style={{ width: "auto" }}
                          value={drive.status ?? "Upcoming"}
                          onChange={(e) => handleStatusChange(drive, e.target.value)}
                        >
                          {STATUSES.map((s) => (
                            <option key={s} value={s}>{s}</option>
                          ))}
                        </select>
                      </td>
                      <td>
                        <button
                          className="btn btn-sm action-edit me-2"
                          onClick={() => handleEdit(drive)}
                        >
                          Edit
                        </button>
                        {canDeleteDrive(drive) ? (
                          <button
                            className="btn btn-sm action-delete"
                            onClick={() => handleDelete(drive)}
                          >
                            Delete
                          </button>
                        ) : (
                          <span
                            className="btn btn-sm btn-outline-secondary disabled d-inline-flex align-items-center gap-1"
                            title="Completed drives can only be deleted by Admin"
                            style={{ cursor: "not-allowed", opacity: 0.55, fontSize: "0.75rem" }}
                          >
                            <Lock size={11} />
                            Locked
                          </span>
                        )}
                      </td>
                    </tr>
                    {isExpanded && (
                      <tr>
                        <td colSpan="9" className="bg-light">
                          <div className="d-flex flex-wrap gap-4">
                            <div><span className="text-muted">Pre-Placement Talk:</span> {formatDate(drive.pptDate)}</div>
                            <div><span className="text-muted">Resume Selection:</span> {formatDate(drive.resumeSelectionDate)}</div>
                            <div><span className="text-muted">Exam:</span> {formatDate(drive.driveDate)}</div>
                            <div><span className="text-muted">Final Selection:</span> {formatDate(drive.finalSelectionDate)}</div>
                          </div>
                        </td>
                      </tr>
                    )}
                  </Fragment>
                  );
                })}
              </tbody>
            </table>
          )}
        </>
      )}
    </Layout>
  );
}

export default Drives;
