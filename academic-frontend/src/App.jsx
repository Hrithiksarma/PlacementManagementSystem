import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Login       from "./pages/Login.jsx";
import Dashboard   from "./pages/Dashboard.jsx";
import AddStudent  from "./pages/AddStudent.jsx";
import EditStudent from "./pages/EditStudent.jsx";

function PrivateRoute({ children }) {
  const token = localStorage.getItem("academic_token");
  return token ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/" element={
          <PrivateRoute><Dashboard /></PrivateRoute>
        } />
        <Route path="/add-student" element={
          <PrivateRoute><AddStudent /></PrivateRoute>
        } />
        <Route path="/edit-student/:rollNo" element={
          <PrivateRoute><EditStudent /></PrivateRoute>
        } />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
