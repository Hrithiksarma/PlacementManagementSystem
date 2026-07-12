import { Navigate } from "react-router-dom";

// Unlike AdminRoute, this excludes PLACEMENT_OFFICER — officer account
// management is an ADMIN-only capability.
const SuperAdminRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  const role  = localStorage.getItem("role");

  if (!token)          return <Navigate to="/login" replace />;
  if (role !== "ADMIN") return <Navigate to="/admin/dashboard" replace />;

  return children;
};

export default SuperAdminRoute;
