import { Navigate } from "react-router-dom";

const StaffRoute = ({ children }) => {
  const token = localStorage.getItem("token");
  const role  = localStorage.getItem("role");

  if (!token)            return <Navigate to="/login" replace />;
  if (role !== "STAFF")  return <Navigate to="/login" replace />;

  return children;
};

export default StaffRoute;
