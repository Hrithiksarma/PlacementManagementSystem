package com.pmrs.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Job categories and the one-time upgradation policy.
 *
 * Tiers are assigned from the CTC offered:
 *   A: 0 – 5.99 LPA · B: 6.0 – 11.99 LPA · C: ≥ 12.0 LPA
 *
 * A placed student may participate further only if the new CTC clears the
 * category's threshold, and upgradation is a one-time option:
 *   A: new CTC  >  1.5 × current CTC
 *   B: new CTC  ≥  max(1.5 × current CTC, "greater than 12 LPA")
 *   C: new CTC  >  2.5 × current CTC
 */
public final class TierPolicy {

    private static final BigDecimal SIX     = BigDecimal.valueOf(6);
    private static final BigDecimal TWELVE  = BigDecimal.valueOf(12);
    private static final BigDecimal ONE_5   = new BigDecimal("1.5");
    private static final BigDecimal TWO_5   = new BigDecimal("2.5");

    private TierPolicy() {}

    /** CTC → "A" / "B" / "C"; null CTC → null. */
    public static String tierForCtc(BigDecimal ctc) {
        if (ctc == null) return null;
        if (ctc.compareTo(SIX) < 0)    return "A";
        if (ctc.compareTo(TWELVE) < 0) return "B";
        return "C";
    }

    /** A=1, B=2, C=3; anything else (null, "Unplaced", legacy) = 0. */
    public static int rank(String tier) {
        if (tier == null) return 0;
        return switch (tier) {
            case "A" -> 1;
            case "B" -> 2;
            case "C" -> 3;
            default  -> 0;
        };
    }

    /**
     * Why a student holding ONE accepted offer may not apply to a drive —
     * or null when the upgradation policy allows it. The one-time check
     * (no third offer, ever) belongs to the caller, which knows the
     * accepted-offer count.
     *
     * @param currentCtc  CTC of the student's accepted offer (may be null)
     * @param newCtc      CTC of the drive being applied to (may be null)
     * @param newTierFallback tier to assume for the drive when newCtc is null
     */
    public static String upgradationBlockReason(BigDecimal currentCtc,
                                                BigDecimal newCtc,
                                                String newTierFallback) {
        if (newCtc == null && rank(newTierFallback) == 0) {
            return "This drive has no CTC recorded — placed students cannot apply"
                 + " until the placement office sets the package.";
        }
        if (currentCtc == null) {
            return "Your accepted offer has no CTC recorded — contact the placement"
                 + " office before applying further.";
        }

        String currentTier = tierForCtc(currentCtc);
        String newTier     = newCtc != null ? tierForCtc(newCtc) : newTierFallback;

        if (rank(newTier) < rank(currentTier)) {
            return "You cannot apply to a Tier " + newTier + " company below your"
                 + " current Tier " + currentTier + " placement.";
        }
        if (rank(newTier) == rank(currentTier)) {
            return "You are already placed in a Tier " + currentTier + " company —"
                 + " you cannot apply to another Tier " + currentTier + " company.";
        }
        if (newCtc == null) {
            return "This drive has no CTC recorded — placed students cannot apply"
                 + " until the placement office sets the package.";
        }

        return switch (currentTier) {
            case "A" -> {
                BigDecimal threshold = mul(currentCtc, ONE_5);
                yield newCtc.compareTo(threshold) > 0 ? null
                        : "Your current offer is " + plain(currentCtc) + " LPA (Tier A) — a new"
                        + " offer must exceed " + plain(threshold) + " LPA (1.5× your current CTC).";
            }
            case "B" -> {
                BigDecimal th15 = mul(currentCtc, ONE_5);
                if (th15.compareTo(TWELVE) >= 0) {
                    yield newCtc.compareTo(th15) >= 0 ? null
                            : "Your current offer is " + plain(currentCtc) + " LPA (Tier B) — a new"
                            + " offer must be at least " + plain(th15) + " LPA (1.5× your current CTC).";
                }
                yield newCtc.compareTo(TWELVE) > 0 ? null
                        : "Your current offer is " + plain(currentCtc) + " LPA (Tier B) — a new"
                        + " offer must be greater than 12 LPA.";
            }
            case "C" -> {
                BigDecimal threshold = mul(currentCtc, TWO_5);
                yield newCtc.compareTo(threshold) > 0 ? null
                        : "Your current offer is " + plain(currentCtc) + " LPA (Tier C) — a new"
                        + " offer must exceed " + plain(threshold) + " LPA (2.5× your current CTC).";
            }
            default -> null; // unreachable: currentCtc non-null always maps to A/B/C
        };
    }

    /** The message shown once the one-time upgradation has been consumed. */
    public static String upgradationUsedMessage() {
        return "Upgradation is a one-time option — you have already used your upgradation.";
    }

    private static BigDecimal mul(BigDecimal a, BigDecimal factor) {
        return a.multiply(factor).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    private static String plain(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }
}
