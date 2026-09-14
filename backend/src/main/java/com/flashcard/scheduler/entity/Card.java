package com.flashcard.scheduler.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A single flashcard. Carries both the learning content (front/back) and the
 * live SM-2 scheduling state (easeFactor, repetitions, interval, dueDate).
 *
 * Design decision: we keep the SM-2 state fields directly on Card (rather than in a
 * separate "SchedulingState" table) because there is exactly one active scheduling
 * state per card at any time -- a 1:1 relationship gains us nothing by splitting tables,
 * and keeping it inline avoids an extra join on the hottest read path (loading due cards).
 * Full history of every review is preserved separately in {@link ReviewLog}.
 */
@Entity
@Table(name = "cards", indexes = {
        @Index(name = "idx_card_deck", columnList = "deck_id"),
        @Index(name = "idx_card_due_date", columnList = "due_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String back;

    @Column(length = 500)
    private String hint;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "card_tags",
            joinColumns = @JoinColumn(name = "card_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ---- SM-2 scheduling state ----

    /** Ease factor, SM-2 default 2.5, clamped to a floor of 1.3 by the scheduling engine. */
    @Column(name = "ease_factor", nullable = false)
    @Builder.Default
    private double easeFactor = 2.5;

    /** Number of consecutive successful (quality >= 3) repetitions. Resets to 0 on failure. */
    @Column(name = "repetitions", nullable = false)
    @Builder.Default
    private int repetitions = 0;

    /** Current inter-repetition interval, in days. */
    @Column(name = "interval_days", nullable = false)
    @Builder.Default
    private int intervalDays = 0;

    /** Date this card is next due for review. Null / today for brand-new cards. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "last_reviewed_at")
    private Instant lastReviewedAt;

    @Column(name = "total_reviews", nullable = false)
    @Builder.Default
    private int totalReviews = 0;

    @Column(name = "lapses", nullable = false)
    @Builder.Default
    private int lapses = 0; // number of times this card was forgotten (quality < 3)

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (dueDate == null) {
            dueDate = LocalDate.now(); // new cards are immediately due
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public boolean isDue() {
        return dueDate == null || !dueDate.isAfter(LocalDate.now());
    }
}
