import { useEffect, useState } from "react";
import { CircleCheckBig } from "lucide-react";
import Layout from "../components/Layout";
import { getPenalties, liftPenalty } from "../services/penaltyService";

const TYPE_META = {
  SKIP_DRIVES:   { label: "Skip Next Drives", bg: "#fffbeb", color: "#92400e" },
  TIME_BAN:      { label: "Time Ban",         bg: "#fff7ed", color: "#9a3412" },
  PERMANENT_BAN: { label: "Permanent Bar",    bg: "#fff1f2", color: "#be123c" },
};

function formatDate(ds) {
  if (!ds) return "—";
  return new Date(ds).toLocaleDateString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
  });
}

function Penalties() {
  const [penalties, setPenalties] = useState([]);
  const [loading,   setLoading]   = useState(true);
  const [error,     setError]     = useState(null);
  const [message,   setMessage]   = useState(null);
  const [lifting,   setLifting]   = useState(null);
  const [showAll,   setShowAll]   = useState(false);

  const load = () => {
    setLoading(true);
    getPenalties()
      .then((res) => { setPenalties(res.data); setLoading(false); })
      .catch(() => { setError("Failed to load penalties."); setLoading(false); });
  };

  useEffect(() => { load(); }, []);

  useEffect(() => {
    if (!message) return;
    const t = setTimeout(() => setMessage(null), 4000);
    return () => clearTimeout(t);
  }, [message]);

  const handleLift = async (p) => {
    if (!window.confirm(`Lift this penalty for ${p.studentName}? This restores their access to drives.`)) return;
    setLifting(p.penaltyId);
    try {
      await liftPenalty(p.penaltyId);
      setMessage({ type: "success", text: `Penalty lifted for ${p.studentName}.` });
      load();
    } catch (err) {
      setMessage({
        type: "error",
        text: err.response?.data?.message ?? "Failed to lift penalty.",
      });
    } finally {
      setLifting(null);
    }
  };

  const displayed = showAll ? penalties : penalties.filter((p) => p.active);
  const activeCount = penalties.filter((p) => p.active).length;
  const referralCount = penalties.filter((p) => p.active && p.disciplinaryReferral).length;

  const remainingText = (p) => {
    if (p.penaltyType === "SKIP_DRIVES")   return `${p.drivesRemaining ?? 0} drive(s) left`;
    if (p.penaltyType === "TIME_BAN")      return `until ${formatDate(p.barUntilDate)}`;
    if (p.penaltyType === "PERMANENT_BAN") return "permanent";
    return "—";
  };

  return (
    <Layout>
      <div className="d-flex align-items-center mb-3">
        <h2 className="mb-0">Penalties</h2>
        <span
          className="ms-3 badge bg-secondary-subtle text-secondary-emphasis fw-normal"
          style={{ fontSize: "0.72rem", padding: "4px 10px", borderRadius: 6 }}
        >
          Withdrawal &amp; offer-decline penalties, issued automatically
        </span>
        <div className="ms-auto d-flex align-items-center gap-3">
          {referralCount > 0 && (
            <span className="badge bg-danger">{referralCount} disciplinary referral(s)</span>
          )}
          <label className="d-flex align-items-center gap-1" style={{ fontSize: "0.85rem" }}>
            <input type="checkbox" checked={showAll}
                   onChange={(e) => setShowAll(e.target.checked)} />
            Show lifted/expired
          </label>
        </div>
      </div>

      {message && (
        <div className={`alert alert-${message.type === "success" ? "success" : "danger"} py-2`}>
          {message.text}
        </div>
      )}
      {error && <div className="alert alert-danger py-2">{error}</div>}

      {loading ? (
        <p className="text-muted">Loading…</p>
      ) : displayed.length === 0 ? (
        <div className="text-center py-5 text-muted">
          <CircleCheckBig size={40} color="#16a34a" style={{ marginBottom: 4 }} />
          <p className="fw-semibold mt-2 mb-1 text-dark">
            {showAll ? "No penalties on record" : "No active penalties"}
          </p>
          <p className="small">Penalties are issued automatically when a student withdraws mid-process or declines an accepted offer.</p>
        </div>
      ) : (
        <>
          <p className="text-muted small mb-2">
            Showing <strong>{displayed.length}</strong> penalt{displayed.length === 1 ? "y" : "ies"}
            {!showAll && ` (${activeCount} active)`}
          </p>
          <table className="table table-striped table-bordered table-hover table-sm">
            <thead className="table-dark">
              <tr>
                <th>ID</th>
                <th>Student</th>
                <th>Roll No</th>
                <th>Type</th>
                <th>Remaining</th>
                <th>Reason</th>
                <th>Issued</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {displayed.map((p) => {
                const meta = TYPE_META[p.penaltyType] ?? {};
                return (
                  <tr key={p.penaltyId} style={!p.active ? { opacity: 0.6 } : {}}>
                    <td>{p.penaltyId}</td>
                    <td className="fw-semibold">
                      {p.studentName ?? "—"}
                      {p.disciplinaryReferral && (
                        <span className="badge bg-danger ms-2" style={{ fontSize: "0.65rem" }}>
                          Disciplinary Referral
                        </span>
                      )}
                    </td>
                    <td>{p.rollNo ?? "—"}</td>
                    <td>
                      <span style={{
                        background: meta.bg, color: meta.color, fontWeight: 600,
                        padding: "3px 10px", borderRadius: 6, fontSize: "0.75rem",
                        whiteSpace: "nowrap",
                      }}>
                        {meta.label ?? p.penaltyType}
                      </span>
                    </td>
                    <td style={{ whiteSpace: "nowrap" }}>{remainingText(p)}</td>
                    <td style={{ fontSize: "0.82rem" }}>{p.reason ?? "—"}</td>
                    <td style={{ fontSize: "0.82rem", whiteSpace: "nowrap" }}>
                      {formatDate(p.createdAt)}
                      <div className="text-muted">by {p.issuedBy}</div>
                    </td>
                    <td>
                      {p.active ? (
                        <span className="badge bg-warning text-dark">Active</span>
                      ) : p.liftedBy ? (
                        <span className="badge bg-success" title={`Lifted by ${p.liftedBy} on ${formatDate(p.liftedAt)}`}>
                          Lifted
                        </span>
                      ) : (
                        <span className="badge bg-secondary">Expired</span>
                      )}
                    </td>
                    <td>
                      {p.active && (
                        <button
                          className="btn btn-sm btn-outline-success"
                          disabled={lifting === p.penaltyId}
                          onClick={() => handleLift(p)}
                        >
                          {lifting === p.penaltyId ? "…" : "Lift"}
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </>
      )}
    </Layout>
  );
}

export default Penalties;
