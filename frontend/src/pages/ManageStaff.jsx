import { useEffect, useState } from "react";
import { CheckCircle2 } from "lucide-react";
import Layout from "../components/Layout";
import {
  getStaffAccount,
  createStaff,
  resetStaffPassword,
  disableStaff,
  enableStaff,
  sendStaffWelcomeEmail,
} from "../services/staffService";

function ManageStaff() {
  const [account, setAccount] = useState(null);
  const [loading, setLoading] = useState(true);

  const [form, setForm]           = useState({ username: "", email: "" });
  const [creating, setCreating]   = useState(false);
  const [createError, setCreateError] = useState(null);

  const [resetting, setResetting]   = useState(false);
  const [resetError, setResetError] = useState(null);

  // Holds the plaintext temp password shown right after a create/reset, plus
  // the welcome-email UI state for it — cleared once the admin navigates away.
  const [revealed, setRevealed]         = useState(null);
  const [comment, setComment]           = useState("");
  const [sendingEmail, setSendingEmail] = useState(false);
  const [emailSent, setEmailSent]       = useState(false);
  const [emailError, setEmailError]     = useState(null);

  const load = () => {
    setLoading(true);
    getStaffAccount()
      .then((res) => setAccount(res.status === 204 ? null : res.data))
      .catch(() => setAccount(null))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const resetRevealState = () => {
    setComment("");
    setEmailSent(false);
    setEmailError(null);
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    setCreateError(null);
    resetRevealState();
    setCreating(true);
    try {
      const res = await createStaff(form.username.trim(), form.email.trim());
      setRevealed(res.data);
      setForm({ username: "", email: "" });
      load();
    } catch (err) {
      setCreateError(err.response?.data?.message ?? "Failed to create the staff portal account.");
    } finally {
      setCreating(false);
    }
  };

  const handleResetPassword = async () => {
    setResetError(null);
    resetRevealState();
    setResetting(true);
    try {
      const res = await resetStaffPassword();
      setRevealed(res.data);
      load();
    } catch (err) {
      setResetError(err.response?.data?.message ?? "Failed to reset the staff portal password.");
    } finally {
      setResetting(false);
    }
  };

  const handleSendWelcomeEmail = async () => {
    setSendingEmail(true);
    setEmailError(null);
    try {
      await sendStaffWelcomeEmail(revealed.userId, revealed.temporaryPassword, comment.trim());
      setEmailSent(true);
    } catch (err) {
      setEmailError(err.response?.data?.message ?? "Failed to send the welcome email.");
    } finally {
      setSendingEmail(false);
    }
  };

  const toggleEnabled = async () => {
    const action = account.enabled ? disableStaff : enableStaff;
    try {
      await action(account.userId);
      load();
    } catch (err) {
      alert(err.response?.data?.message ?? "Failed to update the staff portal account.");
    }
  };

  return (
    <Layout>
      <h2 className="mb-1">Staff Access</h2>
      <p className="text-muted mb-3" style={{ fontSize: "0.9rem" }}>
        A single, shared read-only login for department staff to view placement dashboard
        stats. There is only ever one staff account — its credentials are shared across
        every department that needs visibility.
      </p>

      {loading ? (
        <div className="text-muted">Loading…</div>
      ) : !account ? (
        <div className="card mb-4">
          <div className="card-header fw-semibold">Create Staff Portal Account</div>
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
          </div>
        </div>
      ) : (
        <div className="card mb-4">
          <div className="card-header fw-semibold">Staff Portal Account</div>
          <div className="card-body">
            <table className="table table-borderless mb-3" style={{ maxWidth: 480 }}>
              <tbody>
                <tr>
                  <td className="text-muted">Username</td>
                  <td className="fw-semibold">{account.username}</td>
                </tr>
                <tr>
                  <td className="text-muted">Email</td>
                  <td>{account.email}</td>
                </tr>
                <tr>
                  <td className="text-muted">Status</td>
                  <td>
                    <span className={`badge ${account.enabled ? "bg-success" : "bg-secondary"}`}>
                      {account.enabled ? "Enabled" : "Disabled"}
                    </span>
                  </td>
                </tr>
                <tr>
                  <td className="text-muted">Password</td>
                  <td>
                    {account.mustChangePassword
                      ? <span className="text-muted" style={{ fontSize: "0.85rem" }}>Not yet set by staff</span>
                      : <span className="text-muted" style={{ fontSize: "0.85rem" }}>Set</span>}
                  </td>
                </tr>
              </tbody>
            </table>

            <div className="d-flex gap-2">
              <button
                className={`btn btn-sm ${account.enabled ? "btn-outline-danger" : "btn-outline-success"}`}
                onClick={toggleEnabled}
              >
                {account.enabled ? "Disable" : "Enable"}
              </button>
              <button
                className="btn btn-sm btn-outline-primary"
                onClick={handleResetPassword}
                disabled={resetting}
              >
                {resetting ? "Resetting…" : "Reset Password"}
              </button>
            </div>
            {resetError && <div className="alert alert-danger mt-3 py-2">{resetError}</div>}
          </div>
        </div>
      )}

      {revealed && (
        <div className="card mb-4">
          <div className="card-body">
            <div className="alert alert-success mb-3">
              Credentials for <strong>{revealed.username}</strong>.
              <div className="mt-1">
                Temporary password:{" "}
                <span style={{ fontFamily: "monospace", fontWeight: 700 }}>
                  {revealed.temporaryPassword}
                </span>
              </div>
              <div className="text-muted mt-1" style={{ fontSize: "0.85rem" }}>
                This is shown only once — relay it to whichever departments need access now.
                Whoever logs in first will be prompted to set a new shared password.
              </div>
            </div>

            <div className="fw-semibold mb-2">Admin Comments</div>
            <textarea
              className="form-control mb-3"
              rows="3"
              placeholder="Optional note to include in the welcome email…"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
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
                Sent to {revealed.email}.
              </div>
            )}
          </div>
        </div>
      )}
    </Layout>
  );
}

export default ManageStaff;
