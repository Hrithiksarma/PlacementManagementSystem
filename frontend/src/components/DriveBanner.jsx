import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getEligibleDrives } from "../services/studentPortalService";
import "./DriveBanner.css";

// Job-category tiers A/B/C (by CTC) — same colours as the Eligible Drives page.
const TIER_DOT = {
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

function DriveBanner() {
  const [drives, setDrives] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    getEligibleDrives()
      .then(res => {
        const open = res.data.filter(
          d => !d.alreadyApplied &&
               d.driveStatus !== "Completed" &&
               (d.eligible ?? true)
        );
        setDrives(open);
      })
      .catch(() => {});
  }, []);

  if (drives.length === 0) return null;

  const items = [...drives, ...drives];
  const speed = `${Math.max(20, drives.length * 5)}s`;

  const goToDrive = (driveId) => {
    navigate("/student/eligible-drives", { state: { highlightDriveId: driveId } });
  };

  return (
    <div className="db-bar">
      <div className="db-label">
        <span className="db-pulse" />
        Open Drives
      </div>
      <div className="db-divider" />
      <div className="db-viewport">
        <div className="db-track" style={{ animationDuration: speed }}>
          {items.map((d, i) => (
            <button
              key={`${d.driveId}-${i}`}
              className="db-item"
              onClick={() => goToDrive(d.driveId)}
              title={`Apply to ${d.companyName} — ${d.roleOffered}`}
            >
              {d.companyTier && (
                <span
                  className="db-tier-dot"
                  style={{ background: TIER_DOT[d.companyTier] ?? "#94a3b8" }}
                />
              )}
              <span className="db-company">{d.companyName}</span>
              <span className="db-sep">·</span>
              <span className="db-role">{d.roleOffered}</span>
              {d.packageLpa != null && (
                <>
                  <span className="db-sep">·</span>
                  <span className="db-pkg">{d.packageLpa} LPA</span>
                </>
              )}
              {d.driveDate && (
                <>
                  <span className="db-sep">·</span>
                  <span className="db-date">
                    {new Date(d.driveDate).toLocaleDateString("en-IN", {
                      day: "2-digit", month: "short"
                    })}
                  </span>
                </>
              )}
              <span className="db-bullet">&#8226;</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

export default DriveBanner;
