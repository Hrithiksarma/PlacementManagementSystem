import { Link, useLocation } from "react-router-dom";
import { getRole } from "../services/authService";

const ALL_ITEMS = [
  // ── Admin / Placement Officer ──────────────────────────────────────────────
  { path: "/admin/dashboard",    label: "Dashboard",    icon: "📊", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/students",     label: "Students",     icon: "👥", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/companies",    label: "Companies",    icon: "🏢", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/drives",       label: "Drives",       icon: "📋", roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/applications", label: "Applications", icon: "📄", roles: ["ADMIN", "PLACEMENT_OFFICER"] },

  // ── Student portal ─────────────────────────────────────────────────────────
  { path: "/student/dashboard",        label: "Dashboard",       icon: "📊", roles: ["STUDENT"] },
  { path: "/student/profile",          label: "My Profile",      icon: "👤", roles: ["STUDENT"] },
  { path: "/student/eligible-drives",  label: "Eligible Drives", icon: "🎯", roles: ["STUDENT"] },
  { path: "/student/applications",     label: "My Applications", icon: "📋", roles: ["STUDENT"] },
  { path: "/student/placement-status", label: "Placement Status",icon: "🏆", roles: ["STUDENT"] },
];

function Sidebar() {
  const location = useLocation();
  const role     = getRole();

  const items = ALL_ITEMS.filter((item) => item.roles.includes(role));

  return (
    <div
      className="bg-light border-end d-flex flex-column"
      style={{ width: 210, minHeight: "100vh", flexShrink: 0 }}
    >
      <div className="p-3 pt-4">
        <p
          className="mb-3"
          style={{
            fontSize: "0.62rem",
            fontWeight: 700,
            color: "#94a3b8",
            textTransform: "uppercase",
            letterSpacing: "0.07em",
            paddingLeft: 4,
          }}
        >
          {role === "STUDENT" ? "Student Portal" : "Navigation"}
        </p>

        <ul className="nav flex-column gap-1">
          {items.map((item) => {
            const active =
              item.path === "/admin/dashboard" || item.path === "/student/dashboard"
                ? location.pathname === item.path
                : location.pathname.startsWith(item.path);

            return (
              <li key={item.path} className="nav-item">
                <Link
                  className="nav-link d-flex align-items-center gap-2"
                  to={item.path}
                  style={{
                    fontSize: "0.84rem",
                    fontWeight: active ? 700 : 500,
                    color: active ? "#2563eb" : "#475569",
                    background: active ? "#eff6ff" : "transparent",
                    borderRadius: 7,
                    padding: "7px 10px",
                    transition: "background 0.12s ease, color 0.12s ease",
                  }}
                >
                  <span style={{ fontSize: "1rem" }}>{item.icon}</span>
                  {item.label}
                  {active && (
                    <span
                      style={{
                        marginLeft: "auto",
                        width: 4,
                        height: 4,
                        borderRadius: "50%",
                        background: "#2563eb",
                      }}
                    />
                  )}
                </Link>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
}

export default Sidebar;
