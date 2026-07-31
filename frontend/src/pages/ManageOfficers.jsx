import { useEffect, useState } from "react";
import { CheckCircle2 } from "lucide-react";
import Layout from "../components/Layout";
import {
  getOfficers,
  createOfficer,
  disableOfficer,
  enableOfficer,
  sendOfficerWelcomeEmail,
} from "../services/officerService";

function ManageOfficers() {
  const [officers, setOfficers] = useState([]);
  const [loading, setLoading]   = useState(true);

  const [form, setForm]           = useState({ username: "", email: "" });
  const [creating, setCreating]   = useState(false);
  const [createError, setCreateError] = useState(null);
  const [created, setCreated]     = useState(null);
  const [officerComment, setOfficerComment] = useState("");
  const [sendingEmail, setSendingEmail]     = useState(false);
  const [emailSent, setEmailSent]           = useState(false);
  const [emailError, setEmailError]         = useState(null);

  const load = () => {
    setLoading(true);
    getOfficers()
      .then((res) => setOfficers(res.data))
      .catch(() => setOfficers([]))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreateError(null);
    setCreated(null);
    setOfficerComment("");
    setEmailSent(false);
    setEmailError(null);
    setCreating(true);
    try {
      const res = await createOfficer(form.username.trim(), form.email.trim());
      setCreated(res.data);
      setForm({ username: "", email: "" });
      load();
    } catch (err) {
      setCreateError(err.response?.data?.message ?? "Failed to create officer account.");
    } finally {
      setCreating(false);
    }
  };

  const handleSendWelcomeEmail = async () => {
    setSendingEmail(true);
    setEmailError(null);
    try {
      await sendOfficerWelcomeEmail(created.userId, created.temporaryPassword, officerComment.trim());
      setEmailSent(true);
    } catch (err) {
      setEmailError(err.response?.data?.message ?? "Failed to send the welcome email.");
    } finally {
      setSendingEmail(false);
    }
  };

  const toggleEnabled = async (officer) => {
    const action = officer.enabled ? disableOfficer : enableOfficer;
    try {
      await action(officer.userId);
      load();
    } catch (err) {
      alert(err.response?.data?.message ?? "Failed to update officer account.");
    }
  };

  return (
    <Layout>
      <h2 className="mb-3">Manage Placement Officers</h2>

      <div className="card mb-4">
        <div className="card-header fw-semibold">Create Officer Account</div>
        <div className="card-body">
          <form onSubmit={handleCreate} className="row g-2 align-items-end">
            <div className="col-md-4">
              <label className="form-label">Username</label>
              <input
                className="form-control"
                value={form.username}
                onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))}
                required
              />
            </div>
            <div className="col-md-5">
              <label className="form-label">Email</label>
              <input
                type="email"
                className="form-control"
                value={form.email}
                onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                required
              />
            </div>
            <div className="col-md-3">
              <button className="btn btn-primary w-100" disabled={creating}>
                {creating ? "Creating…" : "Create Account"}
              </button>
            </div>
          </form>

          {createError && <div className="alert alert-danger mt-3 py-2">{createError}</div>}

          {created && (
            <div className="alert alert-success mt-3">
              Account created for <strong>{created.username}</strong>.
              <div className="mt-1">
                Temporary password:{" "}
                <span style={{ fontFamily: "monospace", fontWeight: 700 }}>
                  {created.temporaryPassword}
                </span>
              </div>
              <div className="text-muted mt-1" style={{ fontSize: "0.85rem" }}>
                This is shown only once — relay it to the officer now. They'll be
                prompted to set their own password on first login.
              </div>
            </div>
          )}

          {created && (
            <div className="card mt-3">
              <div className="card-body">
                <div className="fw-semibold mb-2">Admin Comments</div>
                <textarea
                  className="form-control mb-3"
                  rows="3"
                  placeholder="Optional note to include in the welcome email…"
                  value={officerComment}
                  onChange={(e) => setOfficerComment(e.target.value)}
                  disabled={emailSent}
                />
                {emailError && <div className="alert alert-danger py-2">{emailError}</div>}
                <button
                  className="btn btn-primary d-inline-flex align-items-center gap-1"
                  onClick={handleSendWelcomeEmail}
                  disabled={sendingEmail || emailSent}
                >
                  {emailSent && <CheckCircle2 size={13} />}
                  {emailSent ? "Email Sent" : sendingEmail ? "Sending…" : "Send Mail"}
                </button>
                {emailSent && (
                  <div className="text-success mt-2" style={{ fontSize: "0.85rem" }}>
                    Sent to {created.email}.
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <div className="card-header fw-semibold">Officer Accounts</div>
        <table className="table table-striped mb-0">
          <thead>
            <tr>
              <th>Username</th>
              <th>Email</th>
              <th>Status</th>
              <th>Password</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {loading ? (
              <tr><td colSpan="5" className="text-center text-muted py-3">Loading…</td></tr>
            ) : officers.length === 0 ? (
              <tr><td colSpan="5" className="text-center text-muted py-3">No officer accounts yet.</td></tr>
            ) : (
              officers.map((o) => (
                <tr key={o.userId}>
                  <td>{o.username}</td>
                  <td>{o.email}</td>
                  <td>
                    <span className={`badge ${o.enabled ? "bg-success" : "bg-secondary"}`}>
                      {o.enabled ? "Enabled" : "Disabled"}
                    </span>
                  </td>
                  <td>
                    {o.mustChangePassword
                      ? <span className="text-muted" style={{ fontSize: "0.82rem" }}>Not yet set</span>
                      : <span className="text-muted" style={{ fontSize: "0.82rem" }}>Set</span>}
                  </td>
                  <td>
                    <button
                      className={`btn btn-sm ${o.enabled ? "btn-outline-danger" : "btn-outline-success"}`}
                      onClick={() => toggleEnabled(o)}
                    >
                      {o.enabled ? "Disable" : "Enable"}
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </Layout>
  );
}

export default ManageOfficers;
