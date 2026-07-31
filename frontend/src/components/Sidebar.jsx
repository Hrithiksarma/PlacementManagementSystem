import { Link, useLocation } from "react-router-dom";
import {
  LayoutDashboard, Users, Building2, Briefcase, CalendarDays, Megaphone,
  FileText, ClipboardList, Scale, ShieldCheck, User, Target, Trophy, Contact,
} from "lucide-react";
import { getRole } from "../services/authService";
import "./Sidebar.css";

const ALL_ITEMS = [
  // ── Admin / Placement Officer ──────────────────────────────────────────────
  { path: "/admin/dashboard",    label: "Dashboard",    icon: LayoutDashboard, roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/students",     label: "Students",     icon: Users,           roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/companies",    label: "Companies",    icon: Building2,       roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/hr-contacts",  label: "HR Contacts",  icon: Contact,         roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/drives",       label: "Drives",       icon: Briefcase,       roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/calendar",     label: "Calendar",     icon: CalendarDays,    roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/notify-students", label: "Notify Students", icon: Megaphone, roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/applications", label: "Applications", icon: FileText,        roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/forms",        label: "Forms",        icon: ClipboardList,   roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/penalties",    label: "Penalties",    icon: Scale,           roles: ["ADMIN", "PLACEMENT_OFFICER"] },
  { path: "/admin/officers",     label: "Officers",     icon: ShieldCheck,     roles: ["ADMIN"] },

  // ── Student portal ─────────────────────────────────────────────────────────
  { path: "/student/dashboard",        label: "Dashboard",        icon: LayoutDashboard, roles: ["STUDENT"] },
  { path: "/student/profile",          label: "My Profile",       icon: User,            roles: ["STUDENT"] },
  { path: "/student/eligible-drives",  label: "Eligible Drives",  icon: Target,          roles: ["STUDENT"] },
  { path: "/student/applications",     label: "My Applications",  icon: ClipboardList,   roles: ["STUDENT"] },
  { path: "/student/placement-status", label: "Placement Status", icon: Trophy,          roles: ["STUDENT"] },
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
            const Icon = item.icon;

            return (
              <li key={item.path} className="nav-item">
                <Link
                  className={`nav-link d-flex align-items-center gap-2 sidebar-link ${active ? "active" : ""}`}
                  to={item.path}
                >
                  <Icon size={16} className="sidebar-icon" />
                  {item.label}
                  {active && <span className="sidebar-dot" />}
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
