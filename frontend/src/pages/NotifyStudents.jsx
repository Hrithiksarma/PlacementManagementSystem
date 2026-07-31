import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { Bell, CheckCircle2 } from "lucide-react";
import Layout from "../components/Layout";
import {
  getAllDrives,
  getEligibleStudentsForDrive,
  notifyStudents,
} from "../services/driveService";
import {
  getReminderSettings,
  updateReminderSettings,
} from "../services/reminderSettingsService";

const EVENT_TYPES = [
  { key: "PPT",              label: "Pre-Placement Talk" },
  { key: "RESUME_SELECTION", label: "Resume Selection" },
  { key: "EXAM",             label: "Exam" },
  { key: "FINAL_SELECTION",  label: "Final Selection" },
];

function NotifyStudents() {
  const location = useLocation();

  const [drives, setDrives]           = useState([]);
  const [driveId, setDriveId]         = useState("");
  const [eventType, setEventType]     = useState("");
  const [students, setStudents]       = useState([]);
  const [loadingStudents, setLoadingStudents] = useState(false);
  const [selectedIds, setSelectedIds] = useState(new Set());
  const [sending, setSending]         = useState(false);
  const [error, setError]             = useState(null);
  const [notice, setNotice]           = useState(null);
  const [reminderSettings, setReminderSettings] = useState(null);
  const [togglingReminder, setTogglingReminder] = useState(false);

  useEffect(() => {
    getAllDrives()
      .then((res) => setDrives(res.data))
      .catch((err) => console.error("Error loading drives:", err));
  }, []);

  useEffect(() => {
    getReminderSettings()
      .then((res) => setReminderSettings(res.data))
      .catch((err) => console.error("Error loading reminder settings:", err));
  }, []);

  const handleToggleAutoReminders = async () => {
    if (!reminderSettings) return;
    const nextEnabled = !reminderSettings.enabled;
    setTogglingReminder(true);
    try {
      const res = await updateReminderSettings(nextEnabled);
      setReminderSettings(res.data);
    } catch (err) {
      console.error("Error updating reminder settings:", err);
    } finally {
      setTogglingReminder(false);
    }
  };

  useEffect(() => {
    const preset = location.state;
    if (preset?.driveId) setDriveId(String(preset.driveId));
    if (preset?.eventType) setEventType(preset.eventType);
  }, [location.state]);

  useEffect(() => {
    if (!driveId || !eventType) {
      setStudents([]);
      setSelectedIds(new Set());
      return;
    }
    setLoadingStudents(true);
    setError(null);
    setNotice(null);
    getEligibleStudentsForDrive(driveId)
      .then((res) => {
        setStudents(res.data);
        setSelectedIds(new Set());
      })
      .catch((err) => {
        console.error("Error loading eligible students:", err);
        setError("Failed to load eligible students.");
      })
      .finally(() => setLoadingStudents(false));
  }, [driveId, eventType]);

  const allSelected = students.length > 0 && students.every((s) => selectedIds.has(s.studentId));

  const toggleSelect = (id) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id); else next.add(id);
      return next;
    });
  };

  const toggleSelectAll = () => {
    setSelectedIds(allSelected ? new Set() : new Set(students.map((s) => s.studentId)));
  };

  const handleSend = async () => {
    const ids = Array.from(selectedIds);
    if (ids.length === 0) return;
    const eventLabel = EVENT_TYPES.find((t) => t.key === eventType)?.label ?? eventType;
    if (!window.confirm(`Send a "${eventLabel}" reminder to ${ids.length} student(s)?`)) return;
    setSending(true);
    setError(null);
    setNotice(null);
    try {
      await notifyStudents(driveId, eventType, ids);
      setNotice(`Reminder sent to ${ids.length} student(s).`);
      setSelectedIds(new Set());
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message ?? "Failed to send reminder.");
    } finally {
      setSending(false);
    }
  };

  const selectedDrive = drives.find((d) => String(d.driveId) === String(driveId));

  return (
    <Layout>
      <h2 className="mb-3">Notify Students</h2>

      {notice && <div className="alert alert-success py-2">{notice}</div>}
      {error  && <div className="alert alert-danger py-2">{error}</div>}

      <div className="card mb-4">
        <div className="card-body d-flex justify-content-between align-items-center">
          <div>
            <div className="fw-semibold d-flex align-items-center gap-2">
              <Bell size={15} />
              Automatic Weekly Reminders
            </div>
            <div className="text-muted" style={{ fontSize: "0.82rem" }}>
              {reminderSettings
                ? `When on, eligible students are emailed automatically ${reminderSettings.daysBefore} day(s) before each drive event.`
                : "Loading…"}
            </div>
          </div>
          <div className="form-check form-switch mb-0">
            <input
              className="form-check-input"
              type="checkbox"
              role="switch"
              style={{ width: "2.5em", height: "1.4em", cursor: "pointer" }}
              checked={reminderSettings?.enabled ?? false}
              disabled={!reminderSettings || togglingReminder}
              onChange={handleToggleAutoReminders}
            />
            <label className="form-check-label ms-2 fw-semibold">
              {reminderSettings?.enabled ? "ON" : "OFF"}
            </label>
          </div>
        </div>
      </div>

      <div className="card mb-4">
        <div className="card-body">
          <div className="row g-3 align-items-end">
            <div className="col-md-6">
              <label className="form-label fw-semibold">Drive</label>
              <select
                className="form-select"
                value={driveId}
                onChange={(e) => setDriveId(e.target.value)}
              >
                <option value="">Select a drive</option>
                {drives.map((d) => (
                  <option key={d.driveId} value={d.driveId}>
                    {d.company?.companyName ?? "—"} — {d.roleOffered}
                  </option>
                ))}
              </select>
            </div>
            <div className="col-md-6">
              <label className="form-label fw-semibold">Event</label>
              <select
                className="form-select"
                value={eventType}
                onChange={(e) => setEventType(e.target.value)}
              >
                <option value="">Select an event</option>
                {EVENT_TYPES.map((t) => (
                  <option key={t.key} value={t.key}>{t.label}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {driveId && eventType && (
        <>
          <div className="d-flex justify-content-between align-items-center mb-2">
            <p className="text-muted mb-0">
              Showing <strong>{students.length}</strong> eligible student(s) for{" "}
              <strong>{selectedDrive?.company?.companyName ?? "this drive"}</strong>
            </p>
            <button
              className="btn btn-success"
              onClick={handleSend}
              disabled={sending || selectedIds.size === 0}
            >
              {sending ? "Sending…" : `Send Reminder (${selectedIds.size})`}
            </button>
          </div>

          {loadingStudents ? (
            <p className="text-muted">Loading…</p>
          ) : students.length === 0 ? (
            <div className="alert alert-warning">No eligible students found for this drive.</div>
          ) : (
            <table className="table table-striped table-bordered table-hover">
              <thead className="table-dark">
                <tr>
                  <th>
                    <input
                      type="checkbox"
                      checked={allSelected}
                      onChange={toggleSelectAll}
                      title="Select all"
                    />
                  </th>
                  <th>Name</th>
                  <th>Roll No</th>
                  <th>Email</th>
                  <th>Department</th>
                  <th>CGPA</th>
                </tr>
              </thead>
              <tbody>
                {students.map((s) => (
                  <tr key={s.studentId}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedIds.has(s.studentId)}
                        onChange={() => toggleSelect(s.studentId)}
                      />
                    </td>
                    <td>{s.name}</td>
                    <td style={{ fontFamily: "monospace" }}>{s.rollNo ?? "—"}</td>
                    <td>{s.email}</td>
                    <td>{s.department?.deptName ?? "—"}</td>
                    <td>{s.cgpa ?? "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </Layout>
  );
}

export default NotifyStudents;
