import { useEffect, useState } from "react";
import { CheckCircle2, AlertTriangle, Search, Plus, Pencil, Trash2, X, Save, Users, Filter } from "lucide-react";
import Layout from "../components/Layout";
import {
  searchHRContactsByCompany,
  getAllHRContactsMerged,
  addHRContact,
  updateHRContact,
  deleteHRContact,
} from "../services/hrContactService";
import {
  addExternalHrContact,
  updateExternalHrContact,
  deleteExternalHrContact,
} from "../services/externalHrContactService";
import { getAllCompanies } from "../services/companyService";
import "./HrContacts.css";

const EMPTY_PRMS_FORM = { companyId: "", hrName: "", hrEmail: "", hrPhone: "", designation: "" };
const EMPTY_EXTERNAL_FORM = { companyName: "", hrName: "", hrEmail: "", hrPhone: "", designation: "" };

function SourceBadge({ result }) {
  if (result.source === "EXTERNAL") {
    return <span className="hrc-badge external">External Contact</span>;
  }
  if (result.source === "GOOGLE_FORM") {
    return <span className="hrc-badge google">Via Google Form</span>;
  }
  return <span className="hrc-badge manual">Manual</span>;
}

function HrContacts() {
  const [companies, setCompanies] = useState([]);
  const [companyNameQuery, setCompanyNameQuery] = useState("");
  const [results, setResults] = useState([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [quickFilter, setQuickFilter] = useState("");
  const [successMessage, setSuccessMessage] = useState(null);
  const [errorMessage, setErrorMessage] = useState(null);

  const [showPrmsForm, setShowPrmsForm] = useState(false);
  const [prmsForm, setPrmsForm] = useState(EMPTY_PRMS_FORM);
  const [editingHrId, setEditingHrId] = useState(null);

  const [showExternalForm, setShowExternalForm] = useState(false);
  const [externalForm, setExternalForm] = useState(EMPTY_EXTERNAL_FORM);
  const [editingExternalId, setEditingExternalId] = useState(null);

  const [saving, setSaving] = useState(false);

  useEffect(() => {
    getAllCompanies()
      .then((res) => setCompanies(res.data))
      .catch((err) => console.error("Error loading companies:", err));
  }, []);

  useEffect(() => {
    if (!successMessage) return;
    const t = setTimeout(() => setSuccessMessage(null), 4000);
    return () => clearTimeout(t);
  }, [successMessage]);

  useEffect(() => {
    if (!errorMessage) return;
    const t = setTimeout(() => setErrorMessage(null), 5000);
    return () => clearTimeout(t);
  }, [errorMessage]);

  const runSearch = async (companyName) => {
    try {
      const res = await searchHRContactsByCompany(companyName);
      setResults(res.data);
      setHasSearched(true);
    } catch (err) {
      setErrorMessage(err.response?.data?.message ?? "Failed to load HR contacts.");
    }
  };

  const handleSearch = () => {
    const name = companyNameQuery.trim();
    if (!name) return;
    setQuickFilter("");
    runSearch(name);
  };

  const handleViewAll = async () => {
    setQuickFilter("");
    try {
      const res = await getAllHRContactsMerged();
      setResults(res.data);
      setHasSearched(true);
    } catch (err) {
      setErrorMessage(err.response?.data?.message ?? "Failed to load HR contacts.");
    }
  };

  const displayedResults = quickFilter.trim()
    ? results.filter((r) => {
        const q = quickFilter.trim().toLowerCase();
        return [r.name, r.email, r.phone, r.companyName, r.designation]
          .some((v) => v && v.toLowerCase().includes(q));
      })
    : results;

  const refreshIfCurrentSearch = (companyName) => {
    if (companyNameQuery.trim() && companyName.trim().toLowerCase() === companyNameQuery.trim().toLowerCase()) {
      runSearch(companyNameQuery.trim());
    }
  };

  // ── PRMS (real company) add/edit ────────────────────────────────────────

  const openAddPrmsForm = () => {
    const matched = companies.find(
      (c) => c.companyName.toLowerCase() === companyNameQuery.trim().toLowerCase()
    );
    setEditingHrId(null);
    setPrmsForm({ ...EMPTY_PRMS_FORM, companyId: matched ? String(matched.companyId) : "" });
    setShowPrmsForm(true);
    setShowExternalForm(false);
  };

  const openEditPrmsForm = (result) => {
    const company = companies.find((c) => c.companyName === result.companyName);
    setEditingHrId(result.hrId);
    setPrmsForm({
      companyId: company ? String(company.companyId) : "",
      hrName: result.name ?? "",
      hrEmail: result.email ?? "",
      hrPhone: result.phone ?? "",
      designation: result.designation ?? "",
    });
    setShowPrmsForm(true);
    setShowExternalForm(false);
  };

  const closePrmsForm = () => {
    setShowPrmsForm(false);
    setEditingHrId(null);
    setPrmsForm(EMPTY_PRMS_FORM);
  };

  const handleSavePrms = async () => {
    if (!prmsForm.companyId || !prmsForm.hrName.trim() || !prmsForm.hrEmail.trim() || !prmsForm.hrPhone.trim()) {
      setErrorMessage("Company, name, email, and phone are required.");
      return;
    }
    setSaving(true);
    const payload = {
      company: { companyId: Number(prmsForm.companyId) },
      hrName: prmsForm.hrName.trim(),
      hrEmail: prmsForm.hrEmail.trim(),
      hrPhone: prmsForm.hrPhone.trim(),
      designation: prmsForm.designation.trim() || null,
    };
    try {
      if (editingHrId) {
        await updateHRContact(editingHrId, payload);
        setSuccessMessage("HR contact updated.");
      } else {
        await addHRContact(payload);
        setSuccessMessage("HR contact added.");
      }
      const company = companies.find((c) => String(c.companyId) === prmsForm.companyId);
      closePrmsForm();
      if (company) refreshIfCurrentSearch(company.companyName);
    } catch (err) {
      setErrorMessage(err.response?.data?.message ?? "Failed to save the HR contact.");
    } finally {
      setSaving(false);
    }
  };

  const handleDeletePrms = async (result) => {
    if (!window.confirm(`Delete HR contact "${result.name}"?`)) return;
    try {
      await deleteHRContact(result.hrId);
      setSuccessMessage("HR contact deleted.");
      runSearch(companyNameQuery.trim());
    } catch (err) {
      setErrorMessage(err.response?.data?.message ?? "Failed to delete the HR contact.");
    }
  };

  // ── External (non-PRMS) add/edit ────────────────────────────────────────

  const openAddExternalForm = () => {
    setEditingExternalId(null);
    setExternalForm({ ...EMPTY_EXTERNAL_FORM, companyName: companyNameQuery.trim() });
    setShowExternalForm(true);
    setShowPrmsForm(false);
  };

  const openEditExternalForm = (result) => {
    setEditingExternalId(result.externalId);
    setExternalForm({
      companyName: result.companyName ?? "",
      hrName: result.name ?? "",
      hrEmail: result.email ?? "",
      hrPhone: result.phone ?? "",
      designation: result.designation ?? "",
    });
    setShowExternalForm(true);
    setShowPrmsForm(false);
  };

  const closeExternalForm = () => {
    setShowExternalForm(false);
    setEditingExternalId(null);
    setExternalForm(EMPTY_EXTERNAL_FORM);
  };

  const handleSaveExternal = async () => {
    if (!externalForm.companyName.trim() || !externalForm.hrName.trim()
        || !externalForm.hrEmail.trim() || !externalForm.hrPhone.trim()) {
      setErrorMessage("Company name, HR name, email, and phone are required.");
      return;
    }
    setSaving(true);
    const payload = {
      companyName: externalForm.companyName.trim(),
      hrName: externalForm.hrName.trim(),
      hrEmail: externalForm.hrEmail.trim(),
      hrPhone: externalForm.hrPhone.trim(),
      designation: externalForm.designation.trim() || null,
    };
    try {
      if (editingExternalId) {
        await updateExternalHrContact(editingExternalId, payload);
        setSuccessMessage("External HR contact updated.");
      } else {
        await addExternalHrContact(payload);
        setSuccessMessage("External HR contact added.");
      }
      const name = externalForm.companyName;
      closeExternalForm();
      refreshIfCurrentSearch(name);
    } catch (err) {
      setErrorMessage(err.response?.data?.message ?? "Failed to save the external HR contact.");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteExternal = async (result) => {
    if (!window.confirm(`Delete external HR contact "${result.name}"?`)) return;
    try {
      await deleteExternalHrContact(result.externalId);
      setSuccessMessage("External HR contact deleted.");
      runSearch(companyNameQuery.trim());
    } catch (err) {
      setErrorMessage(err.response?.data?.message ?? "Failed to delete the external HR contact.");
    }
  };

  return (
    <Layout>

      <div className="d-flex align-items-center mb-3">
        <h2 className="mb-0">HR Contacts</h2>
        <span
          className="ms-3 badge bg-secondary-subtle text-secondary-emphasis fw-normal"
          style={{ fontSize: "0.72rem", padding: "4px 10px", borderRadius: 6 }}
        >
          Search by company — includes external contacts and Google Form submissions
        </span>
      </div>

      {successMessage && (
        <div className="alert alert-success d-flex align-items-center py-2 mb-3" role="alert">
          <CheckCircle2 size={16} className="me-2 flex-shrink-0" />
          <span className="flex-grow-1">{successMessage}</span>
          <button type="button" className="btn-close btn-close-sm" onClick={() => setSuccessMessage(null)} />
        </div>
      )}

      {errorMessage && (
        <div className="alert alert-danger d-flex align-items-center py-2 mb-3" role="alert">
          <AlertTriangle size={16} className="me-2 flex-shrink-0" />
          <span className="flex-grow-1">{errorMessage}</span>
          <button type="button" className="btn-close btn-close-sm" onClick={() => setErrorMessage(null)} />
        </div>
      )}

      {/* ── Search panel ─────────────────────────────────────────────────── */}
      <div className="card mb-3">
        <div className="card-body" style={{ padding: "16px 20px" }}>
          <div className="row g-2 align-items-end">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1 fw-semibold">Company</label>
              <input
                className="form-control form-control-sm"
                list="hrc-company-options"
                placeholder="Type a company name — PRMS or not…"
                value={companyNameQuery}
                onChange={(e) => setCompanyNameQuery(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              />
              <datalist id="hrc-company-options">
                {companies.map((c) => (
                  <option key={c.companyId} value={c.companyName} />
                ))}
              </datalist>
            </div>
            <div className="col-md-2">
              <button
                className="btn btn-primary btn-sm w-100"
                disabled={!companyNameQuery.trim()}
                onClick={handleSearch}
              >
                Search
              </button>
            </div>
            <div className="col-md-2">
              <button
                className="btn btn-outline-dark btn-sm w-100 d-inline-flex align-items-center justify-content-center gap-1"
                onClick={handleViewAll}
              >
                <Users size={14} />
                View All
              </button>
            </div>
            <div className="col-md-2">
              <button
                className="btn btn-outline-primary btn-sm w-100 d-inline-flex align-items-center justify-content-center gap-1"
                onClick={openAddPrmsForm}
              >
                <Plus size={14} />
                Add HR Contact
              </button>
            </div>
            <div className="col-md-2">
              <button
                className="btn btn-outline-secondary btn-sm w-100 d-inline-flex align-items-center justify-content-center gap-1"
                onClick={openAddExternalForm}
              >
                <Plus size={14} />
                Add External HR
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* ── Add / edit PRMS contact ──────────────────────────────────────── */}
      {showPrmsForm && (
        <div className="hrc-form-box mb-3">
          <div className="fw-semibold mb-2" style={{ fontSize: "0.85rem" }}>
            {editingHrId ? "Edit HR Contact" : "Add HR Contact"}
            <span className="text-muted fw-normal ms-2" style={{ fontSize: "0.74rem" }}>
              (tied to a real PRMS company)
            </span>
          </div>
          <div className="row g-2">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Company</label>
              <select
                className="form-select form-select-sm"
                value={prmsForm.companyId}
                onChange={(e) => setPrmsForm({ ...prmsForm, companyId: e.target.value })}
              >
                <option value="">Select…</option>
                {companies.map((c) => (
                  <option key={c.companyId} value={String(c.companyId)}>
                    {c.companyName}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Name</label>
              <input
                className="form-control form-control-sm"
                value={prmsForm.hrName}
                onChange={(e) => setPrmsForm({ ...prmsForm, hrName: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Designation</label>
              <input
                className="form-control form-control-sm"
                value={prmsForm.designation}
                onChange={(e) => setPrmsForm({ ...prmsForm, designation: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Email</label>
              <input
                type="email"
                className="form-control form-control-sm"
                value={prmsForm.hrEmail}
                onChange={(e) => setPrmsForm({ ...prmsForm, hrEmail: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Phone</label>
              <input
                className="form-control form-control-sm"
                value={prmsForm.hrPhone}
                onChange={(e) => setPrmsForm({ ...prmsForm, hrPhone: e.target.value })}
              />
            </div>
          </div>
          <div className="d-flex gap-2 mt-3">
            <button
              className="btn btn-sm btn-primary d-inline-flex align-items-center gap-1"
              disabled={saving}
              onClick={handleSavePrms}
            >
              <Save size={12} />
              {saving ? "Saving…" : "Save"}
            </button>
            <button
              className="btn btn-sm btn-outline-secondary d-inline-flex align-items-center gap-1"
              disabled={saving}
              onClick={closePrmsForm}
            >
              <X size={12} />
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* ── Add / edit external contact ─────────────────────────────────── */}
      {showExternalForm && (
        <div className="hrc-form-box mb-3">
          <div className="fw-semibold mb-2" style={{ fontSize: "0.85rem" }}>
            {editingExternalId ? "Edit External HR Contact" : "Add External HR Contact"}
            <span className="text-muted fw-normal ms-2" style={{ fontSize: "0.74rem" }}>
              (not tied to any PRMS company)
            </span>
          </div>
          <div className="row g-2">
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Company Name</label>
              <input
                className="form-control form-control-sm"
                placeholder="Any company name…"
                value={externalForm.companyName}
                onChange={(e) => setExternalForm({ ...externalForm, companyName: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Name</label>
              <input
                className="form-control form-control-sm"
                value={externalForm.hrName}
                onChange={(e) => setExternalForm({ ...externalForm, hrName: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Designation</label>
              <input
                className="form-control form-control-sm"
                value={externalForm.designation}
                onChange={(e) => setExternalForm({ ...externalForm, designation: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Email</label>
              <input
                type="email"
                className="form-control form-control-sm"
                value={externalForm.hrEmail}
                onChange={(e) => setExternalForm({ ...externalForm, hrEmail: e.target.value })}
              />
            </div>
            <div className="col-md-4">
              <label className="form-label form-label-sm mb-1">Phone</label>
              <input
                className="form-control form-control-sm"
                value={externalForm.hrPhone}
                onChange={(e) => setExternalForm({ ...externalForm, hrPhone: e.target.value })}
              />
            </div>
          </div>
          <div className="d-flex gap-2 mt-3">
            <button
              className="btn btn-sm btn-primary d-inline-flex align-items-center gap-1"
              disabled={saving}
              onClick={handleSaveExternal}
            >
              <Save size={12} />
              {saving ? "Saving…" : "Save"}
            </button>
            <button
              className="btn btn-sm btn-outline-secondary d-inline-flex align-items-center gap-1"
              disabled={saving}
              onClick={closeExternalForm}
            >
              <X size={12} />
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* ── Results ──────────────────────────────────────────────────────── */}
      {hasSearched && results.length > 0 && (
        <div className="mb-2 position-relative" style={{ maxWidth: 340 }}>
          <Filter size={13} className="text-muted" style={{ position: "absolute", left: 10, top: 9 }} />
          <input
            className="form-control form-control-sm"
            style={{ paddingLeft: 30 }}
            placeholder="Filter loaded results by name, email, company…"
            value={quickFilter}
            onChange={(e) => setQuickFilter(e.target.value)}
          />
        </div>
      )}

      {hasSearched && (
        results.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <Search size={40} strokeWidth={1.6} style={{ marginBottom: 4 }} />
            <p className="fw-semibold mt-2 mb-1 text-dark">No HR contacts found</p>
            <p className="small">Nothing on file yet — try adding one.</p>
          </div>
        ) : displayedResults.length === 0 ? (
          <div className="text-center py-5 text-muted">
            <Filter size={32} strokeWidth={1.6} style={{ marginBottom: 4 }} />
            <p className="fw-semibold mt-2 mb-1 text-dark">No matches for that filter</p>
          </div>
        ) : (
          <table className="table table-striped table-hover hrc-table">
            <thead className="table-dark">
              <tr>
                <th>Name</th>
                <th>Designation</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Company</th>
                <th>Source</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayedResults.map((r) => (
                <tr key={r.hrId ? `hr-${r.hrId}` : `ext-${r.externalId}`}>
                  <td>{r.name ?? "—"}</td>
                  <td>{r.designation ?? "—"}</td>
                  <td>{r.email ?? "—"}</td>
                  <td>{r.phone ?? "—"}</td>
                  <td>{r.companyName ?? "—"}</td>
                  <td><SourceBadge result={r} /></td>
                  <td>
                    {r.hrId ? (
                      <div className="hrc-action-row">
                        <button className="hrc-action-btn edit" title="Edit" onClick={() => openEditPrmsForm(r)}>
                          <Pencil size={15} />
                        </button>
                        <button className="hrc-action-btn delete" title="Delete" onClick={() => handleDeletePrms(r)}>
                          <Trash2 size={15} />
                        </button>
                      </div>
                    ) : (
                      <div className="hrc-action-row">
                        <button className="hrc-action-btn edit" title="Edit" onClick={() => openEditExternalForm(r)}>
                          <Pencil size={15} />
                        </button>
                        <button className="hrc-action-btn delete" title="Delete" onClick={() => handleDeleteExternal(r)}>
                          <Trash2 size={15} />
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )
      )}
    </Layout>
  );
}

export default HrContacts;
