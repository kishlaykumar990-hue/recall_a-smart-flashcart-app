package com.flashcard.scheduler.service;

/**
 * A from-scratch implementation of the SM-2 spaced-repetition scheduling algorithm,
 * originally described by Piotr Wozniak (SuperMemo). This class re-derives the algorithm
 * from its published rules rather than porting any existing codebase (e.g. Anki, SuperMemo,
 * or any third-party library) — it depends on nothing but the JDK.
 *
 * ALGORITHM (as implemented here):
 *
 * Inputs to each review:
 *   q  - quality of recall, integer 0..5:
 *          5 = perfect response
 *          4 = correct response after a hesitation
 *          3 = correct response recalled with serious difficulty
 *          2 = incorrect response; where the correct one seemed easy to recall
 *          1 = incorrect response; the correct one remembered
 *          0 = complete blackout
 *   EF - current ease factor (starts at 2.5)
 *   n  - current number of consecutive correct repetitions
 *   I  - current interval, in days
 *
 * Step 1 - Update the ease factor using the canonical SM-2 formula:
 *   EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
 *   EF' is clamped to a minimum of 1.3 (SM-2's documented floor; an ease factor below this
 *   makes intervals shrink pathologically fast and is disallowed by the original algorithm).
 *
 * Step 2 - Determine repetitions and interval:
 *   If q < 3 (the learner failed to recall the card):
 *       - repetitions resets to 0 (the learning sequence for this item starts over)
 *       - interval resets to 1 day (the card is shown again tomorrow)
 *       - EF is still updated per Step 1 (SM-2 penalizes ease even on failure, but the
 *         interval reset is what actually determines urgency of the next repetition)
 *   If q >= 3 (successful recall):
 *       - repetitions increments by 1
 *       - interval is computed based on repetition count:
 *           n == 1 -> I = 1 day
 *           n == 2 -> I = 6 days
 *           n >  2 -> I = round(previous interval * EF')
 *
 * Step 3 - The new due date is today + I days.
 *
 * This class is intentionally pure (no I/O, no Spring annotations, no entity coupling) so it
 * can be unit tested exhaustively without a Spring context or database.
 */
public final class Sm2Algorithm {

    /** SM-2's documented floor for the ease factor. */
    public static final double MIN_EASE_FACTOR = 1.3;

    /** Default ease factor assigned to brand-new cards. */
    public static final double DEFAULT_EASE_FACTOR = 2.5;

    /** Interval (days) used for the first successful repetition. */
    private static final int FIRST_INTERVAL_DAYS = 1;

    /** Interval (days) used for the second successful repetition. */
    private static final int SECOND_INTERVAL_DAYS = 6;

    /** Interval (days) a card is reset to whenever it is forgotten (quality < 3). */
    private static final int LAPSE_INTERVAL_DAYS = 1;

    private Sm2Algorithm() {
        // static utility class, not instantiable
    }

    /**
     * Immutable result of applying one review to a card's prior scheduling state.
     */
    public record SchedulingResult(
            double easeFactor,
            int repetitions,
            int intervalDays
    ) {}

    /**
     * Applies a single SM-2 review step.
     *
     * @param quality        learner's self-assessed recall quality, must be in [0, 5]
     * @param previousEase   ease factor prior to this review (use {@link #DEFAULT_EASE_FACTOR} for new cards)
     * @param previousReps   consecutive successful repetitions prior to this review (0 for new cards)
     * @param previousInterval interval in days prior to this review (0 for new cards)
     * @return the new scheduling state to persist and to derive the next due date from
     * @throws IllegalArgumentException if quality is outside 0..5
     */
    public static SchedulingResult schedule(int quality, double previousEase, int previousReps, int previousInterval) {
        if (quality < 0 || quality > 5) {
            throw new IllegalArgumentException("Quality must be between 0 and 5 inclusive, got: " + quality);
        }
        if (previousEase <= 0) {
            throw new IllegalArgumentException("previousEase must be positive, got: " + previousEase);
        }
        if (previousReps < 0) {
            throw new IllegalArgumentException("previousReps cannot be negative, got: " + previousReps);
        }

        double updatedEase = updateEaseFactor(previousEase, quality);

        if (quality < 3) {
            // Forgotten: restart the learning sequence for this card.
            return new SchedulingResult(updatedEase, 0, LAPSE_INTERVAL_DAYS);
        }

        int newReps = previousReps + 1;
        int newInterval = computeInterval(newReps, previousInterval, updatedEase);
        return new SchedulingResult(updatedEase, newReps, newInterval);
    }

    /**
     * EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02)), clamped to MIN_EASE_FACTOR.
     */
    static double updateEaseFactor(double previousEase, int quality) {
        double delta = 0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02);
        double updated = previousEase + delta;
        return Math.max(MIN_EASE_FACTOR, updated);
    }

    /**
     * Computes the next interval in days given the (already incremented) repetition count.
     */
    static int computeInterval(int repetitionsAfterThisReview, int previousInterval, double updatedEase) {
        if (repetitionsAfterThisReview == 1) {
            return FIRST_INTERVAL_DAYS;
        }
        if (repetitionsAfterThisReview == 2) {
            return SECOND_INTERVAL_DAYS;
        }
        // For n > 2, previousInterval should be at least 1; guard against 0 on defensive input.
        int base = Math.max(previousInterval, 1);
        return (int) Math.round(base * updatedEase);
    }
}
