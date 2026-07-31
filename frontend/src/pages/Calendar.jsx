import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDays, Megaphone } from "lucide-react";
import Layout from "../components/Layout";
import { getAllDrives } from "../services/driveService";
import "./Calendar.css";

const WEEKDAYS = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
const MONTH_NAMES = [
  "January", "February", "March", "April", "May", "June",
  "July", "August", "September", "October", "November", "December",
];

const EVENT_TYPES = [
  { key: "PPT",              dateField: "pptDate",             label: "Pre-Placement Talk", color: "#3b82f6", bg: "#dbeafe", text: "#1d4ed8" },
  { key: "RESUME_SELECTION", dateField: "resumeSelectionDate",  label: "Resume Selection",   color: "#8b5cf6", bg: "#ede9fe", text: "#6d28d9" },
  { key: "EXAM",             dateField: "driveDate",            label: "Exam",               color: "#f97316", bg: "#ffedd5", text: "#c2410c" },
  { key: "FINAL_SELECTION",  dateField: "finalSelectionDate",   label: "Final Selection",    color: "#22c55e", bg: "#dcfce7", text: "#15803d" },
];

const CURRENT_REAL_YEAR = new Date().getFullYear();
const YEAR_OPTIONS = [];
for (let y = CURRENT_REAL_YEAR - 5; y <= CURRENT_REAL_YEAR + 5; y++) YEAR_OPTIONS.push(y);

const pad = (n) => String(n).padStart(2, "0");
const formatYMD = (date) => `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;

function buildMonthGrid(year, month) {
  const firstOfMonth = new Date(year, month, 1);
  const gridStart = new Date(year, month, 1 - firstOfMonth.getDay());
  const weeks = [];
  const cursor = new Date(gridStart);
  for (let w = 0; w < 6; w++) {
    const week = [];
    for (let d = 0; d < 7; d++) {
      week.push(new Date(cursor));
      cursor.setDate(cursor.getDate() + 1);
    }
    weeks.push(week);
  }
  return weeks;
}

function Calendar() {
  const navigate = useNavigate();
  const [drives, setDrives]           = useState([]);
  const [loading, setLoading]         = useState(false);
  const [error, setError]             = useState(null);
  const [cursorDate, setCursorDate]   = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(null);

  useEffect(() => {
    setLoading(true);
    setError(null);
    getAllDrives()
      .then((res) => setDrives(res.data))
      .catch((err) => {
        console.error("Error loading drives:", err);
        setError("Failed to load drives.");
      })
      .finally(() => setLoading(false));
  }, []);

  const year  = cursorDate.getFullYear();
  const month = cursorDate.getMonth();
  const today = formatYMD(new Date());

  const eventsByDate = useMemo(() => {
    const map = new Map();
    for (const drive of drives) {
      for (const type of EVENT_TYPES) {
        const rawDate = drive[type.dateField];
        if (!rawDate) continue;
        const key = rawDate.slice(0, 10); // "yyyy-MM-dd" from ISO date/datetime string
        const list = map.get(key) ?? [];
        list.push({ drive, type });
        map.set(key, list);
      }
    }
    return map;
  }, [drives]);

  const weeks = useMemo(() => buildMonthGrid(year, month), [year, month]);
  const selectedEvents = selectedDate ? (eventsByDate.get(selectedDate) ?? []) : [];

  const goToMonth = (delta) => {
    setCursorDate(new Date(year, month + delta, 1));
  };

  const goToToday = () => {
    setCursorDate(new Date());
    setSelectedDate(today);
  };

  const handleMonthSelect = (e) => {
    setCursorDate(new Date(year, parseInt(e.target.value, 10), 1));
  };

  const handleYearSelect = (e) => {
    setCursorDate(new Date(parseInt(e.target.value, 10), month, 1));
  };

  return (
    <Layout>
      <div className="d-flex justify-content-between align-items-center mb-3">
        <h2 className="calendar-title mb-0 d-flex align-items-center gap-2">
          <CalendarDays size={20} />
          Drive Calendar
        </h2>
        <div className="d-flex gap-3 align-items-center flex-wrap">
          {EVENT_TYPES.map((t) => (
            <span key={t.key} className="d-flex align-items-center gap-1" style={{ fontSize: "0.78rem" }}>
              <span style={{
                display: "inline-block", width: 10, height: 10, borderRadius: "50%",
                background: t.color,
              }} />
              {t.label}
            </span>
          ))}
        </div>
      </div>

      {error && <div className="alert alert-danger py-2">{error}</div>}

      <div className="d-flex justify-content-between align-items-center mb-3 flex-wrap gap-2">
        <div className="d-flex gap-2 align-items-center">
          <button className="btn btn-outline-primary btn-sm" onClick={() => goToMonth(-1)}>‹ Prev</button>
          <button className="btn btn-primary btn-sm" onClick={goToToday}>Today</button>
          <button className="btn btn-outline-primary btn-sm" onClick={() => goToMonth(1)}>Next ›</button>
        </div>
        <div className="d-flex gap-2 align-items-center">
          <select className="form-select form-select-sm calendar-select" value={month} onChange={handleMonthSelect}>
            {MONTH_NAMES.map((m, i) => (
              <option key={m} value={i}>{m}</option>
            ))}
          </select>
          <select className="form-select form-select-sm calendar-select" value={year} onChange={handleYearSelect}>
            {YEAR_OPTIONS.map((y) => (
              <option key={y} value={y}>{y}</option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <p className="text-muted">Loading…</p>
      ) : (
        <div className="row g-3">
          <div className="col-lg-8">
            <div className="calendar-grid">
              {WEEKDAYS.map((d) => (
                <div key={d} className="calendar-weekday">{d}</div>
              ))}
              {weeks.flat().map((date) => {
                const key = formatYMD(date);
                const dayEvents = eventsByDate.get(key) ?? [];
                const inCurrentMonth = date.getMonth() === month;
                const isToday = key === today;
                const isSelected = key === selectedDate;
                return (
                  <div
                    key={key}
                    className={`calendar-cell ${inCurrentMonth ? "" : "calendar-cell-muted"} ${isSelected ? "calendar-cell-selected" : ""}`}
                    onClick={() => setSelectedDate(key)}
                  >
                    <div className={`calendar-date ${isToday ? "calendar-date-today" : ""}`}>
                      {date.getDate()}
                    </div>
                    <div className="d-flex flex-column gap-1 mt-1">
                      {dayEvents.slice(0, 3).map((e, i) => (
                        <div
                          key={i}
                          className="calendar-chip"
                          style={{
                            background: e.type.bg,
                            color: e.type.text,
                            borderLeft: `3px solid ${e.type.color}`,
                          }}
                          title={`${e.drive.company?.companyName ?? "Drive"} — ${e.type.label}`}
                        >
                          {e.drive.company?.companyName ?? "Drive"}
                        </div>
                      ))}
                      {dayEvents.length > 3 && (
                        <div className="text-muted" style={{ fontSize: "0.7rem" }}>
                          +{dayEvents.length - 3} more
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          <div className="col-lg-4">
            <div className="card" style={{ borderRadius: 12, boxShadow: "0 4px 14px rgba(79, 70, 229, 0.08)", border: "1px solid #e5e7eb" }}>
              <div className="card-body">
                <div className="fw-semibold mb-2" style={{ color: "#312e81" }}>
                  {selectedDate ? new Date(selectedDate).toDateString() : "Select a date"}
                </div>
                {!selectedDate ? (
                  <div className="text-muted" style={{ fontSize: "0.85rem" }}>
                    Click a day on the calendar to see its drive events.
                  </div>
                ) : selectedEvents.length === 0 ? (
                  <div className="text-muted" style={{ fontSize: "0.85rem" }}>
                    No drive events on this date.
                  </div>
                ) : (
                  selectedEvents.map((e, i) => (
                    <div key={i} className="mb-3 pb-2" style={{ borderBottom: "1px solid #e2e8f0" }}>
                      <div className="d-flex align-items-center gap-2 mb-1">
                        <span style={{
                          display: "inline-block", width: 10, height: 10, borderRadius: "50%",
                          background: e.type.color,
                        }} />
                        <span className="fw-semibold">{e.type.label}</span>
                      </div>
                      <div>{e.drive.company?.companyName ?? "—"}</div>
                      <div className="text-muted mb-2" style={{ fontSize: "0.82rem" }}>
                        {e.drive.roleOffered ?? "—"}
                      </div>
                      <button
                        className="btn btn-sm btn-outline-primary d-inline-flex align-items-center gap-1"
                        onClick={() => navigate("/admin/notify-students", {
                          state: { driveId: e.drive.driveId, eventType: e.type.key },
                        })}
                      >
                        <Megaphone size={13} />
                        Notify Students
                      </button>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default Calendar;
