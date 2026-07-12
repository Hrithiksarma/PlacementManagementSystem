import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../services/authService";
import "./Login.css";

function IIITGLogo() {
  return (
    <svg
      className="login-logo-svg"
      viewBox="0 0 100 100"
      xmlns="http://www.w3.org/2000/svg"
      aria-label="IIIT Guwahati logo"
    >
      {/* Outer circle */}
      <circle cx="50" cy="50" r="44" fill="none" stroke="#1e3a8a" strokeWidth="6" />

      {/* Ascending signal bars */}
      <rect x="16" y="64" width="11" height="14" rx="1.5" fill="#1e3a8a" />
      <rect x="31" y="52" width="11" height="26" rx="1.5" fill="#1e3a8a" />
      <rect x="46" y="38" width="11" height="40" rx="1.5" fill="#1e3a8a" />

      {/* Tower / antenna (stylised T) */}
      <rect x="68" y="24" width="7" height="54" rx="1.5" fill="#1e3a8a" />
      <rect x="60" y="24" width="23" height="7" rx="1.5" fill="#1e3a8a" />
    </svg>
  );
}

const DEMO_ACCOUNTS = [
  { username: "admin",   password: "admin123",   role: "Admin",             color: "#dc2626" },
  { username: "officer", password: "officer123", role: "Placement Officer", color: "#2563eb" },
  { username: "student", password: "student123", role: "Student",           color: "#16a34a" },
];

function Login() {
  const [form, setForm]       = useState({ username: "", password: "" });
  const [error, setError]     = useState(null);
  const [loading, setLoading] = useState(false);
  const navigate               = useNavigate();

  const field = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.username.trim() || !form.password.trim()) {
      setError("Username and password are required.");
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const res = await login(form.username.trim(), form.password);
      localStorage.setItem("token",    res.data.token);
      localStorage.setItem("role",     res.data.role);
      localStorage.setItem("username", res.data.username);

      const role = res.data.role;
      if (role === "STUDENT") {
        navigate(res.data.mustChangePassword ? "/student/change-password" : "/student/dashboard", { replace: true });
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

  const fillDemo = (acc) => setForm({ username: acc.username, password: acc.password });

  return (
    <div className="login-page">
      <div className="login-card">

        {/* Header */}
        <div className="login-header">
          <IIITGLogo />
          <h1 className="login-title">IIITG PRMS</h1>
          <p className="login-subtitle">Placement Record Management System</p>
          <p className="login-institute">Indian Institute of Information Technology Guwahati</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="login-form">
          <div className="login-field">
            <label className="login-label">Username</label>
            <input
              type="text"
              className="login-input"
              placeholder="Enter username"
              value={form.username}
              onChange={field("username")}
              autoFocus
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
              <span>⚠</span> {error}
            </div>
          )}

          <button type="submit" className="login-btn" disabled={loading}>
            {loading ? "Signing in…" : "Sign In"}
          </button>
        </form>

        {/* Demo accounts */}
        <div className="login-demo">
          <p className="login-demo-label">Demo accounts</p>
          <div className="login-demo-accounts">
            {DEMO_ACCOUNTS.map((acc) => (
              <button
                key={acc.username}
                className="demo-chip"
                style={{ "--chip-color": acc.color }}
                type="button"
                onClick={() => fillDemo(acc)}
              >
                {acc.role}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default Login;
