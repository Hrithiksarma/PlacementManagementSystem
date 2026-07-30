import Navbar from "./Navbar";
import Sidebar from "./Sidebar";
import DriveBanner from "./DriveBanner";
import { getRole } from "../services/authService";

function Layout({ children }) {
  const isStudent = getRole() === "STUDENT";

  return (
    <>
      <Navbar />
      <div className="d-flex">
        <Sidebar />
        <div
          className="container-fluid p-4"
          style={{ paddingBottom: isStudent ? "62px" : "1.5rem" }}
        >
          {children}
        </div>
      </div>
      {isStudent && <DriveBanner />}
    </>
  );
}

export default Layout;
