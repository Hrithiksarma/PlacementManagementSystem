import { Navigate } from "react-router-dom";

const RoleRedirect = () => {
  const role = localStorage.getItem("role");

  if (role === "STUDENT")                                return <Navigate to="/student/dashboard" replace />;
  if (role === "STAFF")                                  return <Navigate to="/staff/dashboard" replace />;
  if (["ADMIN", "PLACEMENT_OFFICER"].includes(role))    return <Navigate to="/admin/dashboard" replace />;

  return <Navigate to="/login" replace />;
};

export default RoleRedirect;
