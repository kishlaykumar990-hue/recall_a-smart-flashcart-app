package com.flashcard.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit record of a single review event. Kept separate from Card so we retain
 * full history (for analytics/retention charts) even though Card only stores current state.
 */
@Entity
@Table(name = "review_logs", indexes = {
        @Index(name = "idx_review_card", columnList = "card_id"),
        @Index(name = "idx_review_user", columnList = "user_id"),
        @Index(name = "idx_review_reviewed_at", columnList = "reviewed_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewLog {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_id", nullable = false)
    private Card card;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Quality score given by the learner, 0-5, per SM-2 semantics. */
    @Column(nullable = false)
    private int quality;

    @Column(name = "ease_factor_before", nullable = false)
    private double easeFactorBefore;

    @Column(name = "ease_factor_after", nullable = false)
    private double easeFactorAfter;

    @Column(name = "interval_before", nullable = false)
    private int intervalBefore;

    @Column(name = "interval_after", nullable = false)
    private int intervalAfter;

    @Column(name = "repetitions_before", nullable = false)
    private int repetitionsBefore;

    @Column(name = "repetitions_after", nullable = false)
    private int repetitionsAfter;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    @PrePersist
    void prePersist() {
        if (reviewedAt == null) reviewedAt = Instant.now();
    }
}
