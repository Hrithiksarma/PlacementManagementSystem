import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { changePassword, logout, getUsername, getRole } from "../services/authService";

function ChangePassword() {
  const navigate = useNavigate();
  const username = getUsername();
  const role     = getRole();
  const homePath = role === "STUDENT" ? "/student/dashboard" : "/admin/dashboard";

  const [form, setForm]       = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [error, setError]     = useState(null);
  const [loading, setLoading] = useState(false);
  const [done, setDone]       = useState(false);

  const field = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (form.newPassword.length < 6) {
      setError("New password must be at least 6 characters.");
      return;
    }
    if (form.newPassword !== form.confirmPassword) {
      setError("New password and confirmation do not match.");
      return;
    }
    if (form.newPassword === form.currentPassword) {
      setError("New password must be different from your temporary password.");
      return;
    }

    setLoading(true);
    try {
      await changePassword(form.currentPassword, form.newPassword);
      setDone(true);
    } catch (err) {
      setError(err.response?.data?.error ?? "Failed to change password. Check your current password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "100vh", background: "#f5f7fa" }}>
      <div className="card shadow-sm" style={{ maxWidth: 440, width: "100%" }}>
        <div className="card-body p-4">
          <h4 className="mb-1">Set a New Password</h4>
          <p className="text-muted" style={{ fontSize: "0.9rem" }}>
            {username ? <>Signed in as <strong>{username}</strong>. </> : null}
            This is your first login — you're using a temporary password.
            Please set a new password before continuing.
          </p>

          {done ? (
            <>
              <div className="alert alert-success">Password updated successfully.</div>
              <button className="btn btn-primary w-100" onClick={() => navigate(homePath, { replace: true })}>
                Continue to Dashboard
              </button>
            </>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Current (Temporary) Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={form.currentPassword}
                  onChange={field("currentPassword")}
                  autoComplete="current-password"
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">New Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={form.newPassword}
                  onChange={field("newPassword")}
                  autoComplete="new-password"
                  minLength={6}
                  required
                />
              </div>
              <div className="mb-3">
                <label className="form-label">Confirm New Password</label>
                <input
                  type="password"
                  className="form-control"
                  value={form.confirmPassword}
                  onChange={field("confirmPassword")}
                  autoComplete="new-password"
                  minLength={6}
                  required
                />
              </div>

              {error && <div className="alert alert-danger py-2">{error}</div>}

              <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                {loading ? "Updating…" : "Update Password"}
              </button>
              <button type="button" className="btn btn-link w-100 mt-1 text-muted" onClick={logout}>
                Log out instead
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}

export default ChangePassword;
