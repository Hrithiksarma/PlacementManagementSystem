import { useState } from "react";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";
import DriveBanner from "./DriveBanner";
import { getRole } from "../services/authService";

function Layout({ children }) {
  const isStudent = getRole() === "STUDENT";
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <>
      <Navbar onToggleSidebar={() => setSidebarOpen((o) => !o)} />
      <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />
      <div
        className="container-fluid p-4"
        style={{ paddingBottom: isStudent ? "62px" : "1.5rem" }}
      >
        {children}
      </div>
      {isStudent && <DriveBanner />}
    </>
  );
}

export default Layout;
