import { useEffect, useState } from "react";
import { useParams, useLocation, useNavigate } from "react-router-dom";
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from "recharts";
import { ArrowLeft, Sparkles, CheckCircle2, AlertCircle, MessageCircleQuestion } from "lucide-react";
import Layout from "../../components/Layout";
import { analyzeResumeMatch, getResumeMatch } from "../../services/resumeMatchService";
import "./ResumeMatchResult.css";

const SCORE_COLOR = (score) =>
  score >= 70 ? "#16a34a" : score >= 40 ? "#d97706" : "#dc2626";

function SectionHeader({ title, sub }) {
  return (
    <div className="rm-section-header">
      <span className="rm-section-title">{title}</span>
      {sub && <span className="rm-section-sub">{sub}</span>}
    </div>
  );
}

function PieTooltip({ active, payload }) {
  if (!active || !payload?.length) return null;
  const { name, value } = payload[0];
  return (
    <div className="rm-tooltip">
      <strong>{name}</strong>: {value} skill{value !== 1 ? "s" : ""}
    </div>
  );
}

function Skeleton({ h = 80 }) {
  return <div className="rm-skeleton" style={{ height: h }} />;
}

function ResumeMatchResult() {
  const { driveId } = useParams();
  const location     = useLocation();
  const navigate      = useNavigate();

  const [result,     setResult]     = useState(location.state?.result ?? null);
  const [loading,    setLoading]    = useState(!location.state?.result);
  const [notAnalyzed,setNotAnalyzed]= useState(false);
  const [analyzing,  setAnalyzing]  = useState(false);
  const [error,      setError]      = useState(null);

  useEffect(() => {
    if (result) return;
    getResumeMatch(driveId)
      .then((res) => { setResult(res.data); setLoading(false); })
      .catch((err) => {
        if (err?.response?.status === 404) setNotAnalyzed(true);
        else setError("Failed to load your analysis.");
        setLoading(false);
      });
  }, [driveId]);

  const runAnalysis = async () => {
    setAnalyzing(true);
    setError(null);
    try {
      const res = await analyzeResumeMatch(driveId);
      setResult(res.data);
      setNotAnalyzed(false);
    } catch (err) {
      const data = err?.response?.data;
      const msg =
        (typeof data === "string" && data ? data : null) ||
        data?.message ||
        data?.error ||
        "Couldn't analyze your fit right now. Please try again.";
      setError(String(msg));
    } finally {
      setAnalyzing(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="rm-container">
          <Skeleton h={24} />
          <Skeleton h={160} />
          <div className="rm-grid">
            <Skeleton h={220} />
            <Skeleton h={220} />
          </div>
        </div>
      </Layout>
    );
  }

  if (notAnalyzed) {
    return (
      <Layout>
        <div className="rm-container">
          <button className="rm-back" onClick={() => navigate(-1)}>
            <ArrowLeft size={13} /> Back
          </button>
          <div className="rm-empty">
            <Sparkles className="rm-empty-icon" size={32} strokeWidth={1.6} />
            <p>You haven't analyzed your fit for this drive yet.</p>
            {error && <p style={{ color: "#dc2626" }}>{error}</p>}
            <button className="rm-reanalyze-btn" disabled={analyzing} onClick={runAnalysis}>
              {analyzing ? "Analyzing…" : "Analyze My Fit"}
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  if (error && !result) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  const skills  = result.skills ?? [];
  const matched = skills.filter((s) => s.status === "MATCHED");
  const missing = skills.filter((s) => s.status === "MISSING");
  const questions = result.interviewQuestions ?? [];

  const pieData = [
    { name: "Matched", value: matched.length, color: "#16a34a" },
    { name: "Missing",  value: missing.length, color: "#d97706" },
  ].filter((d) => d.value > 0);

  return (
    <Layout>
      <div className="rm-container">
        <button className="rm-back" onClick={() => navigate(-1)}>
          <ArrowLeft size={13} /> Back
        </button>

        <div className="rm-header">
          <div>
            <h2 className="rm-title">Skills-Gap Analysis</h2>
            <p className="rm-sub">
              {result.createdAt
                ? `Analyzed ${new Date(result.createdAt).toLocaleString("en-IN")}`
                : "Resume vs. job description"}
            </p>
          </div>
          <button className="rm-reanalyze-btn" disabled={analyzing} onClick={runAnalysis}>
            {analyzing ? "Re-analyzing…" : "Re-analyze"}
          </button>
        </div>

        {error && <div className="alert alert-danger py-2">{error}</div>}

        {/* Score hero — full width */}
        <div className="rm-card rm-hero">
          <SectionHeader title="Match Score" sub={`${skills.length} skills evaluated`} />
          <div className="rm-score-row">
            <div className="rm-score-block">
              <div className="rm-score-value" style={{ color: SCORE_COLOR(result.score) }}>
                {result.score}%
              </div>
              <div className="rm-score-label">
                {matched.length} matched · {missing.length} to build
              </div>
            </div>
            {pieData.length > 0 && (
              <ResponsiveContainer width={180} height={150}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%"
                    innerRadius={42} outerRadius={68} paddingAngle={3} dataKey="value">
                    {pieData.map((e) => <Cell key={e.name} fill={e.color} />)}
                  </Pie>
                  <Tooltip content={<PieTooltip />} />
                  <Legend iconType="circle" iconSize={8}
                    formatter={(value) => <span style={{ fontSize: "0.72rem", color: "#475569" }}>{value}</span>} />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>

        {/* Two-column body */}
        <div className="rm-grid">
          <div className="rm-col">
            <div className="rm-card">
              <SectionHeader title="Skills Breakdown" />
              <div className="rm-skill-group">
                <div className="rm-skill-group-title">
                  <CheckCircle2 size={12} style={{ marginRight: 4, verticalAlign: -1 }} />
                  Matched ({matched.length})
                </div>
                {matched.length === 0 ? (
                  <p style={{ fontSize: "0.8rem", color: "#94a3b8" }}>No overlapping skills found yet.</p>
                ) : (
                  <div className="rm-chips">
                    {matched.map((s) => (
                      <span key={s.skillName} className="rm-chip rm-chip-matched">{s.skillName}</span>
                    ))}
                  </div>
                )}
              </div>
              <div className="rm-skill-group">
                <div className="rm-skill-group-title">
                  <AlertCircle size={12} style={{ marginRight: 4, verticalAlign: -1 }} />
                  To Build ({missing.length})
                </div>
                {missing.length === 0 ? (
                  <p style={{ fontSize: "0.8rem", color: "#94a3b8" }}>Nothing missing — great fit!</p>
                ) : (
                  <div className="rm-chips">
                    {missing.map((s) => (
                      <span key={s.skillName} className="rm-chip rm-chip-missing">{s.skillName}</span>
                    ))}
                  </div>
                )}
              </div>
            </div>
          </div>

          <div className="rm-col">
            <div className="rm-card">
              <SectionHeader title="Your Study Plan" />
              <p className="rm-writeup">{result.writeUp}</p>
            </div>

            {questions.length > 0 && (
              <div className="rm-card">
                <SectionHeader title="Likely Interview Questions" sub="Based on your gaps" />
                <ol className="rm-questions">
                  {questions.map((q, i) => (
                    <li key={i} className="rm-question">
                      <MessageCircleQuestion size={13} className="rm-question-icon" />
                      <span>{q}</span>
                    </li>
                  ))}
                </ol>
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default ResumeMatchResult;
