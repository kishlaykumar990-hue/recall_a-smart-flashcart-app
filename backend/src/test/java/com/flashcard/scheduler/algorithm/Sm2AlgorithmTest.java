package com.flashcard.scheduler.algorithm;

import com.flashcard.scheduler.service.Sm2Algorithm;
import com.flashcard.scheduler.service.Sm2Algorithm.SchedulingResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exhaustive unit tests for the custom SM-2 implementation.
 * Validates every branch of the algorithm described in Sm2Algorithm's javadoc.
 */
class Sm2AlgorithmTest {

    private static final double DELTA = 0.0001;

    @Nested
    @DisplayName("New card, first review")
    class FirstReview {

        @Test
        @DisplayName("Perfect recall (q=5) on a brand new card sets reps=1, interval=1")
        void perfectRecallOnNewCard() {
            SchedulingResult r = Sm2Algorithm.schedule(5, Sm2Algorithm.DEFAULT_EASE_FACTOR, 0, 0);
            assertEquals(1, r.repetitions());
            assertEquals(1, r.intervalDays());
            assertTrue(r.easeFactor() > Sm2Algorithm.DEFAULT_EASE_FACTOR, "ease should increase on q=5");
        }

        @Test
        @DisplayName("Quality exactly 3 (borderline pass) still counts as success")
        void borderlinePassCounts() {
            SchedulingResult r = Sm2Algorithm.schedule(3, Sm2Algorithm.DEFAULT_EASE_FACTOR, 0, 0);
            assertEquals(1, r.repetitions());
            assertEquals(1, r.intervalDays());
        }

        @Test
        @DisplayName("Failure (q=2) on a new card keeps repetitions at 0")
        void failureOnNewCard() {
            SchedulingResult r = Sm2Algorithm.schedule(2, Sm2Algorithm.DEFAULT_EASE_FACTOR, 0, 0);
            assertEquals(0, r.repetitions());
            assertEquals(1, r.intervalDays());
        }
    }

    @Nested
    @DisplayName("Interval progression across repetitions")
    class IntervalProgression {

        @Test
        @DisplayName("1st success -> 1 day, 2nd success -> 6 days, 3rd success -> round(6*EF)")
        void classicProgression() {
            SchedulingResult first = Sm2Algorithm.schedule(4, 2.5, 0, 0);
            assertEquals(1, first.intervalDays());
            assertEquals(1, first.repetitions());

            SchedulingResult second = Sm2Algorithm.schedule(4, first.easeFactor(), first.repetitions(), first.intervalDays());
            assertEquals(6, second.intervalDays());
            assertEquals(2, second.repetitions());

            SchedulingResult third = Sm2Algorithm.schedule(4, second.easeFactor(), second.repetitions(), second.intervalDays());
            int expected = (int) Math.round(6 * second.easeFactor());
            assertEquals(expected, third.intervalDays());
            assertEquals(3, third.repetitions());
        }

        @Test
        @DisplayName("Intervals grow monotonically under repeated perfect recall")
        void intervalsGrowUnderRepeatedSuccess() {
            double ease = Sm2Algorithm.DEFAULT_EASE_FACTOR;
            int reps = 0;
            int interval = 0;
            int previousInterval = -1;
            for (int i = 0; i < 8; i++) {
                SchedulingResult r = Sm2Algorithm.schedule(5, ease, reps, interval);
                if (i >= 2) { // after the fixed 1/6 day steps, growth should be strictly increasing
                    assertTrue(r.intervalDays() > previousInterval,
                            "interval should strictly grow at repetition " + i);
                }
                ease = r.easeFactor();
                reps = r.repetitions();
                previousInterval = interval;
                interval = r.intervalDays();
            }
        }
    }

    @Nested
    @DisplayName("Ease factor updates")
    class EaseFactorUpdates {

        @ParameterizedTest(name = "quality={0} should change ease by the documented SM-2 delta")
        @CsvSource({
                "5, 0.1",
                "4, 0.0",
                "3, -0.14",
                "2, -0.32",
                "1, -0.54",
                "0, -0.8"
        })
        void easeDeltaMatchesFormula(int quality, double expectedDelta) {
            double before = 2.5;
            SchedulingResult r = Sm2Algorithm.schedule(quality, before, 3, 6); // reps>=1 so ease math is isolated
            assertEquals(before + expectedDelta, r.easeFactor(), DELTA);
        }

        @Test
        @DisplayName("Ease factor never drops below the 1.3 floor even under repeated failures")
        void easeFactorClampedAtFloor() {
            double ease = 1.35;
            for (int i = 0; i < 10; i++) {
                SchedulingResult r = Sm2Algorithm.schedule(0, ease, 0, 0);
                assertTrue(r.easeFactor() >= Sm2Algorithm.MIN_EASE_FACTOR,
                        "ease factor must never go below " + Sm2Algorithm.MIN_EASE_FACTOR);
                ease = r.easeFactor();
            }
            assertEquals(Sm2Algorithm.MIN_EASE_FACTOR, ease, DELTA);
        }
    }

    @Nested
    @DisplayName("Lapse behaviour (forgetting a learned card)")
    class LapseBehaviour {

        @Test
        @DisplayName("A well-learned card that is forgotten resets repetitions and interval")
        void forgettingResetsProgress() {
            // Simulate a card that has been reviewed successfully several times.
            SchedulingResult learned = Sm2Algorithm.schedule(5, 2.6, 4, 20);
            assertEquals(5, learned.repetitions());

            // Now the learner forgets it entirely.
            SchedulingResult lapsed = Sm2Algorithm.schedule(1, learned.easeFactor(), learned.repetitions(), learned.intervalDays());
            assertEquals(0, lapsed.repetitions(), "repetitions must reset to 0 on failure");
            assertEquals(1, lapsed.intervalDays(), "interval must reset to 1 day on failure");
            assertTrue(lapsed.easeFactor() < learned.easeFactor(), "ease factor should still be penalized on failure");
        }
    }

    @Nested
    @DisplayName("Input validation")
    class InputValidation {

        @ParameterizedTest
        @ValueSource(ints = {-1, 6, 10, -100})
        @DisplayName("Quality outside [0,5] throws IllegalArgumentException")
        void invalidQualityRejected(int badQuality) {
            assertThrows(IllegalArgumentException.class,
                    () -> Sm2Algorithm.schedule(badQuality, 2.5, 0, 0));
        }

        @Test
        @DisplayName("Non-positive previous ease factor is rejected")
        void invalidEaseRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> Sm2Algorithm.schedule(4, 0.0, 0, 0));
            assertThrows(IllegalArgumentException.class,
                    () -> Sm2Algorithm.schedule(4, -1.0, 0, 0));
        }

        @Test
        @DisplayName("Negative previous repetitions is rejected")
        void invalidRepsRejected() {
            assertThrows(IllegalArgumentException.class,
                    () -> Sm2Algorithm.schedule(4, 2.5, -1, 0));
        }
    }

    @Test
    @DisplayName("Full realistic learning trajectory matches hand-computed SM-2 values")
    void handComputedTrajectory() {
        // Card reviewed with quality sequence: 5, 5, 5, 2 (forgets), 4
        double ease = Sm2Algorithm.DEFAULT_EASE_FACTOR;
        int reps = 0, interval = 0;

        SchedulingResult r1 = Sm2Algorithm.schedule(5, ease, reps, interval);
        assertEquals(1, r1.intervalDays());
        assertEquals(2.6, r1.easeFactor(), DELTA);

        SchedulingResult r2 = Sm2Algorithm.schedule(5, r1.easeFactor(), r1.repetitions(), r1.intervalDays());
        assertEquals(6, r2.intervalDays());
        assertEquals(2.7, r2.easeFactor(), DELTA);

        SchedulingResult r3 = Sm2Algorithm.schedule(5, r2.easeFactor(), r2.repetitions(), r2.intervalDays());
        assertEquals((int) Math.round(6 * 2.8), r3.intervalDays());
        assertEquals(2.8, r3.easeFactor(), DELTA);

        // Learner forgets completely.
        SchedulingResult r4 = Sm2Algorithm.schedule(2, r3.easeFactor(), r3.repetitions(), r3.intervalDays());
        assertEquals(0, r4.repetitions());
        assertEquals(1, r4.intervalDays());
        assertEquals(2.8 - 0.32, r4.easeFactor(), DELTA);

        // Recovers with a good-not-perfect review.
        SchedulingResult r5 = Sm2Algorithm.schedule(4, r4.easeFactor(), r4.repetitions(), r4.intervalDays());
        assertEquals(1, r5.repetitions());
        assertEquals(1, r5.intervalDays());
        assertEquals(r4.easeFactor(), r5.easeFactor(), DELTA); // q=4 => delta 0.0
    }
}
