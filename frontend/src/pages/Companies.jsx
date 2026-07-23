import { useState } from "react";
import Layout from "../components/Layout";
import { getAllCompanies, addCompany, updateCompany, deleteCompany } from "../services/companyService";
import { getAllHRContacts } from "../services/hrContactService";
import { isAdmin } from "../services/authService";
import "./Companies.css";

const TIERS = ["All", "A", "B", "C"];
const SECTORS = ["All", "IT", "Finance", "Fintech", "Consulting", "Core", "Analytics"];

// Job categories by CTC: A < 6 LPA · B 6–11.99 LPA · C ≥ 12 LPA
const tierLabelFor = (t) => (t === "All" ? "All Tiers" : t ? `Tier ${t}` : t);

function HRContactsCell({ contacts }) {
  if (!contacts || contacts.length === 0) {
    return <span className="text-muted">—</span>;
  }
  return (
    <div className="d-flex flex-column gap-1">
      {contacts.map((hr) => (
        <div key={hr.hrId} style={{ fontSize: "0.85rem" }}>
          <div className="fw-semibold">{hr.hrName}</div>
          <div className="text-muted">{hr.hrPhone}</div>
        </div>
      ))}
    </div>
  );
}

const emptyForm = {
  companyName: "",
  sector: "",
  tier: "",
  website: "",
};

function Companies() {
  const [allCompanies, setAllCompanies] = useState([]);
  const [hrContactsByCompany, setHrContactsByCompany] = useState({});
  const [hasSearched, setHasSearched] = useState(false);
  const [search, setSearch] = useState("");
  const [filters, setFilters] = useState({ tier: "All", sector: "All" });
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);

  const admin = isAdmin();

  const handleSearch = async () => {
    try {
      const [companiesRes, hrContactsRes] = await Promise.all([
        getAllCompanies(),
        getAllHRContacts(),
      ]);
      setAllCompanies(companiesRes.data);

      const byCompany = {};
      for (const hr of hrContactsRes.data) {
        const companyId = hr.company?.companyId;
        if (!companyId) continue;
        (byCompany[companyId] ??= []).push(hr);
      }
      setHrContactsByCompany(byCompany);

      setHasSearched(true);
    } catch (err) {
      console.error("Error loading companies:", err);
    }
  };

  const handleEdit = (company) => {
    setForm({
      companyName: company.companyName,
      sector: company.sector,
      tier: company.tier,
      website: company.website,
    });
    setEditingId(company.companyId);
    setShowForm(true);
  };

  const handleSaveCompany = async () => {
    try {
      if (editingId) {
        await updateCompany(editingId, form);
        alert("Company Updated Successfully");
      } else {
        await addCompany(form);
        alert("Company Added Successfully");
      }
      if (hasSearched) handleSearch();
      setShowForm(false);
      setEditingId(null);
      setForm(emptyForm);
    } catch (err) {
      console.error(err);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this company?")) return;
    try {
      await deleteCompany(id);
      alert("Company Deleted Successfully");
      if (hasSearched) handleSearch();
    } catch (err) {
      console.error(err);
      alert("Failed to delete company");
    }
  };

  const field = (key) => ({
    value: form[key],
    onChange: (e) => setForm({ ...form, [key]: e.target.value }),
  });

  const displayedCompanies = allCompanies
    .filter((c) => filters.tier === "All" || c.tier === filters.tier)
    .filter((c) => filters.sector === "All" || c.sector === filters.sector)
    .filter((c) => c.companyName.toLowerCase().includes(search.toLowerCase()));

  const tierLabel   = tierLabelFor(filters.tier);
  const sectorLabel = filters.sector === "All" ? "All Sectors" : filters.sector;

  return (
    <Layout>
      <div className="d-flex justify-content-between mb-3">
        <h2>Companies</h2>
        <button
          className="btn btn-primary"
          onClick={() => {
            setShowForm(!showForm);
            if (showForm) { setEditingId(null); setForm(emptyForm); }
          }}
        >
          {showForm ? "Cancel" : "Add Company"}
        </button>
      </div>

      {/* Filter Panel */}
      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-3">
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
            <div className="col-md-3">
              <label className="form-label fw-semibold">Sector</label>
              <select
                className="form-select"
                value={filters.sector}
                onChange={(e) => setFilters({ ...filters, sector: e.target.value })}
              >
                {SECTORS.map((s) => (
                  <option key={s} value={s}>{s}</option>
                ))}
              </select>
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary w-100" onClick={handleSearch}>
                Load Companies
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
                <label className="form-label">Company Name</label>
                <input className="form-control" placeholder="Company name" {...field("companyName")} />
              </div>
              <div className="col-md-6">
                <label className="form-label">Sector</label>
                <select className="form-select" {...field("sector")}>
                  <option value="">Select sector</option>
                  {SECTORS.filter((s) => s !== "All").map((s) => (
                    <option key={s} value={s}>{s}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Tier</label>
                <select className="form-select" {...field("tier")}>
                  <option value="">Select tier</option>
                  {TIERS.filter((t) => t !== "All").map((t) => (
                    <option key={t} value={t}>{tierLabelFor(t)}</option>
                  ))}
                </select>
              </div>
              <div className="col-md-6">
                <label className="form-label">Website</label>
                <input className="form-control" placeholder="https://..." {...field("website")} />
              </div>
            </div>
            <button className="btn btn-success mt-3" onClick={handleSaveCompany}>
              {editingId ? "Update Company" : "Save Company"}
            </button>
          </div>
        </div>
      )}

      {/* Results */}
      {hasSearched && (
        <>
          <div className="mb-3">
            <input
              type="text"
              className="form-control"
              placeholder="Search company..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <p className="text-muted mb-2">
            Showing <strong>{displayedCompanies.length}</strong> company(s) — {tierLabel} • {sectorLabel}
            {!admin && (
              <span className="badge bg-info text-white ms-2" style={{ fontSize: "0.7rem" }}>
                Delete restricted
              </span>
            )}
          </p>

          {displayedCompanies.length === 0 ? (
            <div className="alert alert-warning">
              No companies found for selected filters.
            </div>
          ) : (
            <table className="table table-striped table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>ID</th>
                  <th>Company Name</th>
                  <th>Sector</th>
                  <th>Tier</th>
                  <th>Website</th>
                  <th>HR Contact</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {displayedCompanies.map((company) => (
                  <tr key={company.companyId}>
                    <td>{company.companyId}</td>
                    <td>{company.companyName}</td>
                    <td>{company.sector}</td>
                    <td>{tierLabelFor(company.tier)}</td>
                    <td>{company.website}</td>
                    <td>
                      <HRContactsCell contacts={hrContactsByCompany[company.companyId]} />
                    </td>
                    <td>
                      <button
                        className="btn btn-sm action-edit me-2"
                        onClick={() => handleEdit(company)}
                      >
                        Edit
                      </button>
                      {admin && (
                        <button
                          className="btn btn-sm action-delete"
                          onClick={() => handleDelete(company.companyId)}
                        >
                          Delete
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </Layout>
  );
}

export default Companies;
