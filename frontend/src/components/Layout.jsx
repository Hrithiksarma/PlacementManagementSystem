import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

function Layout({ children }) {
  return (
    <>
      <Navbar />
      <div className="d-flex">
        <Sidebar />
        <div className="container-fluid p-4">
          {children}
        </div>
      </div>
    </>
  );
}

export default Layout;
