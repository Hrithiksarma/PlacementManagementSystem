import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { AlertTriangle } from "lucide-react";
import { login } from "../services/authService";
import iiitgLogo from "../assets/logo_iiit.png";
import "./Login.css";

const PORTALS = [
  { value: "ADMIN",             label: "Admin" },
  { value: "PLACEMENT_OFFICER", label: "Placement Cell" },
  { value: "STUDENT",           label: "Student" },
  { value: "STAFF",             label: "Department Staff" },
];
const PORTAL_LABEL = Object.fromEntries(PORTALS.map((p) => [p.value, p.label]));

function Login() {
  const [form, setForm]       = useState({ portal: "", username: "", password: "" });
  const [error, setError]     = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate               = useNavigate();

  const field = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.portal) {
      setError("Select which portal you're logging into.");
      return;
    }
    if (!form.username.trim() || !form.password.trim()) {
      setError("Username and password are required.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await login(form.username.trim(), form.password);
      const role = res.data.role;

      if (role !== form.portal) {
        setError(
          `This account isn't a ${PORTAL_LABEL[form.portal]} account — ` +
          `it belongs to the ${PORTAL_LABEL[role] ?? role} portal.`
        );
        return;
      }

      localStorage.setItem("token",    res.data.token);
      localStorage.setItem("role",     res.data.role);
      localStorage.setItem("username", res.data.username);

      if (role === "STUDENT") {
        navigate(res.data.mustChangePassword ? "/student/change-password" : "/student/dashboard", { replace: true });
      } else if (role === "STAFF") {
        navigate(res.data.mustChangePassword ? "/staff/change-password" : "/staff/dashboard", { replace: true });
      } else if (["ADMIN", "PLACEMENT_OFFICER"].includes(role)) {
        navigate(res.data.mustChangePassword ? "/admin/change-password" : "/admin/dashboard", { replace: true });
      } else {
        navigate("/login");
      }
    } catch (err) {
      setError(err.response?.data?.error ?? "Invalid username or password.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">

        {/* Header */}
        <div className="login-header">
          <img src={iiitgLogo} alt="IIIT Guwahati" className="login-logo" />
          <h1 className="login-title">IIITG PRMS</h1>
          <p className="login-subtitle">Placement Record Management System</p>
          <p className="login-institute">Indian Institute of Information Technology Guwahati</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="login-field">
            <label className="login-label">Portal</label>
            <select
              className="login-input login-select"
              value={form.portal}
              onChange={field("portal")}
              autoFocus
              required
            >
              <option value="" disabled>Select portal</option>
              {PORTALS.map((p) => (
                <option key={p.value} value={p.value}>{p.label}</option>
              ))}
            </select>
          </div>

          <div className="login-field">
            <label className="login-label">Username</label>
            <input
              type="text"
              className="login-input"
              placeholder="Enter username"
              value={form.username}
              onChange={field("username")}
              autoComplete="username"
            />
          </div>

          <div className="login-field">
            <label className="login-label">Password</label>
            <input
              type="password"
              className="login-input"
              placeholder="Enter password"
              value={form.password}
              onChange={field("password")}
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className="login-error">
              <AlertTriangle size={14} /> {error}
            </div>
          )}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? "Signing in…" : "Sign In"}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;
