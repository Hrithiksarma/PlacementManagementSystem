import { Navigate } from "react-router-dom";

const ADMIN_ROLES = ["ADMIN", "PLACEMENT_OFFICER"];

const AdminRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  const role  = localStorage.getItem("role");

  if (!token)                      return <Navigate to="/login" replace />;
  if (!ADMIN_ROLES.includes(role)) return <Navigate to="/student/dashboard" replace />;

  return children;
};

export default AdminRoute;
