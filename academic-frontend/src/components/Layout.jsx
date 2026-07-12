import { useNavigate } from "react-router-dom";

export default function Layout({ children }) {
  const navigate  = useNavigate();
  const username  = localStorage.getItem("academic_user") ?? "Admin";

  const handleLogout = () => {
    localStorage.removeItem("academic_token");
    localStorage.removeItem("academic_user");
    navigate("/login");
  };

  return (
    <>
      <header className="header">
        <div className="header-logo">
          🎓 Academic ERP
          <span>IIIT Guwahati</span>
        </div>
        <div className="header-right">
          <span>👤 {username}</span>
          <button className="btn-logout" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>
      <main className="main">{children}</main>
    </>
  );
}
