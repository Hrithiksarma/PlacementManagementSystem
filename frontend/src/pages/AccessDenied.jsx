import { useNavigate } from "react-router-dom";
import { getRole } from "../services/authService";
import "./AccessDenied.css";

const ROLE_LABEL = {
  ADMIN:             "Admin",
  PLACEMENT_OFFICER: "Placement Officer",
  STUDENT:           "Student",
};

function AccessDenied() {
  const navigate  = useNavigate();
  const role      = getRole();
  const roleLabel = ROLE_LABEL[role] ?? role;

  return (
    <div className="access-denied-page">
      <div className="access-denied-card">
        <div className="access-denied-icon">🔒</div>
        <h1 className="access-denied-title">Access Denied</h1>
        <p className="access-denied-message">
          Your account (<strong>{roleLabel}</strong>) does not have permission to view
          this page.
        </p>
        <p className="access-denied-hint">
          If you believe this is a mistake, please contact your system administrator.
        </p>
        <div className="access-denied-actions">
          <button className="btn-go-dashboard" onClick={() => navigate("/")}>
            ← Go to Dashboard
          </button>
          <button className="btn-go-back" onClick={() => navigate(-1)}>
            Go Back
          </button>
        </div>
      </div>
    </div>
  );
}

export default AccessDenied;
