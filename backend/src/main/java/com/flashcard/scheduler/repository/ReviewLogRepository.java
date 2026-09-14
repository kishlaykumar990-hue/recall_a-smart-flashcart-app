package com.flashcard.scheduler.repository;

import com.flashcard.scheduler.entity.ReviewLog;
import com.flashcard.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, UUID> {

    List<ReviewLog> findByUserOrderByReviewedAtDesc(User user);

    @Query("""
           SELECT r FROM ReviewLog r
           WHERE r.user = :user AND r.reviewedAt >= :since
           ORDER BY r.reviewedAt ASC
           """)
    List<ReviewLog> findRecentByUser(@Param("user") User user, @Param("since") Instant since);

    long countByUserAndCardId(User user, UUID cardId);
}
