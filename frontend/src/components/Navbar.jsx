import { logout, getUsername, getRole } from "../services/authService";

const ROLE_LABEL = {
  ADMIN:              "Admin",
  PLACEMENT_OFFICER:  "Placement Officer",
  STUDENT:            "Student",
};

const ROLE_COLOR = {
  ADMIN:             "#dc2626",
  PLACEMENT_OFFICER: "#2563eb",
  STUDENT:           "#16a34a",
};

function Navbar() {
  const username = getUsername();
  const role     = getRole();

  return (
    <nav className="navbar navbar-dark bg-dark px-3" style={{ minHeight: 52 }}>
      <span className="navbar-brand mb-0 fw-bold" style={{ letterSpacing: "-0.01em" }}>
        🎓 PRMS — Placement Record Management System
      </span>

      {username && (
        <div className="d-flex align-items-center gap-3">
          <div className="d-flex align-items-center gap-2">
            <span
              style={{
                width: 30, height: 30, borderRadius: "50%",
                background: ROLE_COLOR[role] ?? "#475569",
                display: "flex", alignItems: "center", justifyContent: "center",
                fontWeight: 700, fontSize: "0.75rem", color: "#fff",
                flexShrink: 0,
              }}
            >
              {username.charAt(0).toUpperCase()}
            </span>
            <div style={{ lineHeight: 1.2 }}>
              <div style={{ fontSize: "0.82rem", color: "#f1f5f9", fontWeight: 600 }}>
                {username}
              </div>
              <div style={{ fontSize: "0.68rem", color: "#94a3b8" }}>
                {ROLE_LABEL[role] ?? role}
              </div>
            </div>
          </div>

          <button
            onClick={logout}
            className="btn btn-outline-light btn-sm"
            style={{ fontSize: "0.75rem", padding: "4px 12px" }}
          >
            Logout
          </button>
        </div>
      )}
    </nav>
  );
}

export default Navbar;
