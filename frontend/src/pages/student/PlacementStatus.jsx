import { useEffect, useState } from "react";
import { Swiper, SwiperSlide } from "swiper/react";
import { Pagination } from "swiper/modules";
import "swiper/css";
import "swiper/css/pagination";
import Layout from "../../components/Layout";
import {
  getPlacementStatus,
  getPlacementOffers,
} from "../../services/studentPortalService";
import "./PlacementStatus.css";

const TIER_COLOR = {
  C: "#7c3aed",
  B: "#16a34a",
  A: "#2563eb",
};

const TIER_LABEL = { A: "Tier A", B: "Tier B", C: "Tier C" };

const PIPELINE = ["Applied", "First Round", "Interview Scheduled", "Selected"];

function formatDate(ds) {
  if (!ds) return "—";
  return new Date(ds).toLocaleDateString("en-IN", {
    day: "2-digit", month: "long", year: "numeric",
  });
}

function ProgressTracker({ currentStage }) {
  const currentIdx = PIPELINE.indexOf(currentStage);
  const isRejected = currentStage === "Rejected";
  const allDone    = currentStage === "Selected";

  return (
    <div className="ps-tracker-card">
      <div className="ps-tracker-header">
        <div className="ps-tracker-title">Application Progress</div>
        {isRejected && (
          <span className="ps-tracker-rejected-badge">Not Progressed</span>
        )}
        {allDone && (
          <span className="ps-tracker-placed-badge">🎉 Placed!</span>
        )}
      </div>

      <div className="ps-tracker-steps">
        {PIPELINE.map((stage, i) => {
          const done   = allDone || (!isRejected && i < currentIdx);
          const active = !allDone && !isRejected && i === currentIdx;
          const cls    = done ? "ps-ts-done" : active ? "ps-ts-active" : "ps-ts-future";
          return (
            <div key={stage} className="ps-tracker-col">
              <div className={`ps-ts ${cls}`}>
                <div className="ps-ts-dot">{done ? "✓" : i + 1}</div>
                <div className="ps-ts-label">{stage}</div>
              </div>
              {i < PIPELINE.length - 1 && (
                <div className={`ps-ts-connector ${done ? "ps-ts-conn-done" : ""}`} />
              )}
            </div>
          );
        })}
      </div>

      {isRejected && (
        <div className="ps-tracker-rejected-note">
          Your application was not shortlisted at this time. Keep applying to other drives!
        </div>
      )}
    </div>
  );
}

function OfferCard({ offer }) {
  const tierColor = TIER_COLOR[offer.companyTier] ?? "#374151";
  return (
    <div className={`ps-offer-card ${offer.currentOffer ? "ps-offer-card--current" : "ps-offer-card--previous"}`}>
      {/* Badge */}
      <div className={`ps-offer-label ${offer.currentOffer ? "ps-offer-label--current" : "ps-offer-label--previous"}`}>
        {offer.currentOffer ? "⭐ Current Best Offer" : "Previous Offer"}
      </div>

      {/* Company + tier */}
      <div className="ps-offer-company">{offer.companyName ?? "—"}</div>
      {offer.companyTier && (
        <span className="ps-tier-badge" style={{ background: tierColor }}>
          {TIER_LABEL[offer.companyTier] ?? offer.companyTier}
        </span>
      )}

      {/* Details */}
      <div className="ps-offer-grid">
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Role</div>
          <div className="ps-offer-item-value">{offer.roleOffered ?? "—"}</div>
        </div>
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Package</div>
          <div className={`ps-offer-item-value ${offer.currentOffer ? "ps-offer-pkg-current" : "ps-offer-pkg-prev"}`}>
            {offer.packageLpa != null ? `${offer.packageLpa} LPA` : "—"}
          </div>
        </div>
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Date</div>
          <div className="ps-offer-item-value">{formatDate(offer.selectionDate)}</div>
        </div>
        <div className="ps-offer-item">
          <div className="ps-offer-item-label">Status</div>
          <div className="ps-offer-item-value">
            <span className="ps-offer-selected-chip">✓ Selected</span>
          </div>
        </div>
      </div>
    </div>
  );
}

function PlacementStatus() {
  const [status,  setStatus]  = useState(null);
  const [offers,  setOffers]  = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState(null);

  useEffect(() => {
    Promise.all([getPlacementStatus(), getPlacementOffers()])
      .then(([statusRes, offersRes]) => {
        setStatus(statusRes.data);
        setOffers(offersRes.data);
        setLoading(false);
      })
      .catch(() => {
        setError("Failed to load placement status.");
        setLoading(false);
      });
  }, []);

  if (loading) {
    return (
      <Layout>
        <div className="ps-container">
          <div className="ps-skeleton" style={{ height: 140 }} />
          <div className="ps-skeleton" style={{ height: 280 }} />
        </div>
      </Layout>
    );
  }

  if (error) {
    return <Layout><div className="alert alert-danger m-3">{error}</div></Layout>;
  }

  const hasActiveApplication = status?.currentStage && status.currentStage !== "Not Applied";

  return (
    <Layout>
      <div className="ps-container">

        {/* ── Progress tracker ─────────────────────────────────────────────── */}
        {hasActiveApplication && (
          <ProgressTracker currentStage={status.currentStage} />
        )}

        {/* ── Pending offer (Selected — awaiting student decision) ──────── */}
        {!status?.placed && status?.hasPendingOffer && (
          <div className="ps-pending-offer-card">
            <div className="ps-pending-offer-header">
              <span className="ps-pending-offer-icon">📩</span>
              <div>
                <div className="ps-pending-offer-title">Offer Available — Action Required</div>
                <div className="ps-pending-offer-sub">
                  {status.pendingOfferCompany}
                  {status.pendingOfferTier && (
                    <span className="ps-tier-badge ms-2"
                          style={{ background: TIER_COLOR[status.pendingOfferTier] ?? "#374151" }}>
                      {TIER_LABEL[status.pendingOfferTier] ?? status.pendingOfferTier}
                    </span>
                  )}
                </div>
              </div>
            </div>
            <div className="ps-pending-offer-meta">
              {status.pendingOfferRole && <span>{status.pendingOfferRole}</span>}
              {status.pendingOfferPackage != null && <span>💰 {status.pendingOfferPackage} LPA</span>}
              {status.pendingOfferDeadline && (
                <span style={{ color: "#dc2626" }}>⏰ Valid until {formatDate(status.pendingOfferDeadline)}</span>
              )}
            </div>
            <p className="ps-pending-offer-hint">
              Go to <strong>My Applications</strong> to accept or reject this offer.
            </p>
          </div>
        )}

        {status?.placed ? (
          /* ── PLACED — offer carousel ─────────────────────────────────────── */
          <>
            <div className="ps-placed-banner">
              <div className="ps-confetti-row">🎉 🎊 🏆 🎊 🎉</div>
              <div className="ps-placed-headline">Congratulations!</div>
              <div className="ps-placed-sub">
                {offers.length > 1
                  ? `You have accepted ${offers.length} placement offers. Your best offer is shown first.`
                  : "You have accepted a placement offer."}
              </div>
            </div>

            {offers.length > 0 && (
              <div className="ps-swiper-wrap">
                {offers.length === 1 ? (
                  <OfferCard offer={offers[0]} />
                ) : (
                  <Swiper
                    modules={[Pagination]}
                    pagination={{ clickable: true }}
                    spaceBetween={20}
                    slidesPerView={1}
                    className="ps-swiper"
                  >
                    {offers.map((offer) => (
                      <SwiperSlide key={offer.applicationId}>
                        <OfferCard offer={offer} />
                      </SwiperSlide>
                    ))}
                  </Swiper>
                )}

                {offers.length > 1 && (
                  <p className="ps-swipe-hint">← Swipe to see all offers →</p>
                )}
              </div>
            )}
          </>
        ) : hasActiveApplication ? (
          /* ── IN PROGRESS ─────────────────────────────────────────────────── */
          <div className="ps-pending-card">
            <div className="ps-pending-icon">⏳</div>
            <div className="ps-pending-headline">Placement In Progress</div>
            <div className="ps-pending-sub">
              {status?.message ?? "Your placement journey is in progress. Keep applying!"}
            </div>

            <div className="ps-tips-grid">
              <div className="ps-tip">
                <span className="ps-tip-icon">📋</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Track Your Applications</div>
                  <div className="ps-tip-text">
                    Monitor status updates on your My Applications page.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">🎯</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Apply to More Drives</div>
                  <div className="ps-tip-text">
                    Increase your chances by applying to more eligible drives.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">📈</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Prepare for Interviews</div>
                  <div className="ps-tip-text">
                    Review technical topics relevant to your applied roles.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">✅</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Stay Reachable</div>
                  <div className="ps-tip-text">
                    Keep your contact details updated so the placement cell can reach you.
                  </div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          /* ── NO APPLICATIONS YET ─────────────────────────────────────────── */
          <div className="ps-pending-card">
            <div className="ps-pending-icon">🚀</div>
            <div className="ps-pending-headline">Start Your Journey</div>
            <div className="ps-pending-sub">
              You haven&apos;t applied to any drives yet. Browse eligible drives and submit your first application!
            </div>

            <div className="ps-tips-grid">
              <div className="ps-tip">
                <span className="ps-tip-icon">🎯</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Browse Eligible Drives</div>
                  <div className="ps-tip-text">
                    Check drives you qualify for based on your CGPA and backlog criteria.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">📈</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Improve Your Profile</div>
                  <div className="ps-tip-text">
                    Higher CGPA unlocks Tier B and Tier C company drives.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">✅</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Clear Backlogs</div>
                  <div className="ps-tip-text">
                    Zero active backlogs maximises your eligible drive count.
                  </div>
                </div>
              </div>
              <div className="ps-tip">
                <span className="ps-tip-icon">📋</span>
                <div className="ps-tip-body">
                  <div className="ps-tip-title">Prepare Early</div>
                  <div className="ps-tip-text">
                    Review aptitude, DSA, and company-specific prep materials.
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}

      </div>
    </Layout>
  );
}

export default PlacementStatus;
