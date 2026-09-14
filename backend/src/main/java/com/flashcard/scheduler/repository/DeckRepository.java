package com.flashcard.scheduler.repository;

import com.flashcard.scheduler.entity.Deck;
import com.flashcard.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {
    List<Deck> findByOwnerAndArchivedFalseOrderByUpdatedAtDesc(User owner);
    Optional<Deck> findByIdAndOwner(UUID id, User owner);
}
