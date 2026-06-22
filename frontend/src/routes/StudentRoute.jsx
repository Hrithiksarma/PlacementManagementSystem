import { Navigate } from "react-router-dom";

const StudentRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  const role  = localStorage.getItem("role");

  if (!token)             return <Navigate to="/login" replace />;
  if (role !== "STUDENT") return <Navigate to="/admin/dashboard" replace />;

  return children;
};

export default StudentRoute;
