import { BrowserRouter, Routes, Route } from "react-router-dom";

// Activate the axios interceptors on app load
import "./services/authService";

import Dashboard    from "./pages/Dashboard";
import Companies    from "./pages/Companies";
import Students     from "./pages/Students";
import Drives       from "./pages/Drives";
import Calendar     from "./pages/Calendar";
import NotifyStudents from "./pages/NotifyStudents";
import Applications from "./pages/Applications";
import Login          from "./pages/Login";
import AccessDenied   from "./pages/AccessDenied";
import ChangePassword from "./pages/ChangePassword";
import ManageOfficers from "./pages/ManageOfficers";
import FormSubmissions from "./pages/FormSubmissions";
import Penalties       from "./pages/Penalties";

import StudentDashboard from "./pages/student/StudentDashboard";
import MyProfile        from "./pages/student/MyProfile";
import EligibleDrives   from "./pages/student/EligibleDrives";
import MyApplications   from "./pages/student/MyApplications";
import PlacementStatus  from "./pages/student/PlacementStatus";

import StudentRoute    from "./routes/StudentRoute";
import AdminRoute      from "./routes/AdminRoute";
import SuperAdminRoute from "./routes/SuperAdminRoute";
import RoleRedirect    from "./routes/RoleRedirect";

function App() {
  return (
    <BrowserRouter>
      <Routes>

        {/* ── Public ──────────────────────────────────────────────── */}
        <Route path="/login"         element={<Login />} />
        <Route path="/access-denied" element={<AccessDenied />} />

        {/* ── Root: redirect to role-appropriate dashboard ─────────── */}
        <Route path="/" element={<RoleRedirect />} />

        {/* ── Student portal ───────────────────────────────────────── */}
        <Route path="/student/change-password" element={
          <StudentRoute><ChangePassword /></StudentRoute>
        } />
        <Route path="/student/dashboard" element={
          <StudentRoute><StudentDashboard /></StudentRoute>
        } />
        <Route path="/student/profile" element={
          <StudentRoute><MyProfile /></StudentRoute>
        } />
        <Route path="/student/eligible-drives" element={
          <StudentRoute><EligibleDrives /></StudentRoute>
        } />
        <Route path="/student/applications" element={
          <StudentRoute><MyApplications /></StudentRoute>
        } />
        <Route path="/student/placement-status" element={
          <StudentRoute><PlacementStatus /></StudentRoute>
        } />

        {/* ── Admin / Placement Officer ────────────────────────────── */}
        <Route path="/admin/change-password" element={
          <AdminRoute><ChangePassword /></AdminRoute>
        } />
        <Route path="/admin/dashboard" element={
          <AdminRoute><Dashboard /></AdminRoute>
        } />
        <Route path="/admin/students" element={
          <AdminRoute><Students /></AdminRoute>
        } />
        <Route path="/admin/companies" element={
          <AdminRoute><Companies /></AdminRoute>
        } />
        <Route path="/admin/drives" element={
          <AdminRoute><Drives /></AdminRoute>
        } />
        <Route path="/admin/calendar" element={
          <AdminRoute><Calendar /></AdminRoute>
        } />
        <Route path="/admin/notify-students" element={
          <AdminRoute><NotifyStudents /></AdminRoute>
        } />
        <Route path="/admin/applications" element={
          <AdminRoute><Applications /></AdminRoute>
        } />
        <Route path="/admin/forms" element={
          <AdminRoute><FormSubmissions /></AdminRoute>
        } />
        <Route path="/admin/penalties" element={
          <AdminRoute><Penalties /></AdminRoute>
        } />
        <Route path="/admin/officers" element={
          <SuperAdminRoute><ManageOfficers /></SuperAdminRoute>
        } />

        {/* ── Catch-all ────────────────────────────────────────────── */}
        <Route path="*" element={<RoleRedirect />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;
