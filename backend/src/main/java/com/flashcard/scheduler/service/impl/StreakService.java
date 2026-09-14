package com.flashcard.scheduler.service.impl;

import com.flashcard.scheduler.entity.User;
import com.flashcard.scheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Maintains each learner's daily study streak. A streak increments once per calendar day
 * the user completes at least one review; it resets to 1 if a day was missed.
 */
@Service
@RequiredArgsConstructor
public class StreakService {

    private final UserRepository userRepository;

    @Transactional
    public void recordStudyActivity(User user) {
        LocalDate today = LocalDate.now();
        LocalDate last = user.getLastStudyDate();

        if (last != null && last.equals(today)) {
            return; // already recorded today, no-op
        }

        if (last != null && last.equals(today.minusDays(1))) {
            user.setCurrentStreak(user.getCurrentStreak() + 1);
        } else {
            user.setCurrentStreak(1); // missed a day (or first ever session): streak restarts
        }

        if (user.getCurrentStreak() > user.getLongestStreak()) {
            user.setLongestStreak(user.getCurrentStreak());
        }

        user.setLastStudyDate(today);
        userRepository.save(user);
    }
}
