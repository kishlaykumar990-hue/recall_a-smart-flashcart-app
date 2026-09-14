package com.flashcard.scheduler.repository;

import com.flashcard.scheduler.entity.Card;
import com.flashcard.scheduler.entity.Deck;
import com.flashcard.scheduler.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByDeck(Deck deck);

    Optional<Card> findByIdAndDeckOwner(UUID id, User owner);

    @Query("""
           SELECT c FROM Card c
           WHERE c.deck.owner = :owner
             AND c.dueDate <= :today
             AND c.deck.archived = false
           ORDER BY c.dueDate ASC
           """)
    List<Card> findDueCardsForUser(@Param("owner") User owner, @Param("today") LocalDate today);

    @Query("""
           SELECT c FROM Card c
           WHERE c.deck = :deck
             AND c.dueDate <= :today
           ORDER BY c.dueDate ASC
           """)
    List<Card> findDueCardsForDeck(@Param("deck") Deck deck, @Param("today") LocalDate today);

    @Query("""
           SELECT COUNT(c) FROM Card c
           WHERE c.deck.owner = :owner AND c.dueDate <= :today AND c.deck.archived = false
           """)
    long countDueForUser(@Param("owner") User owner, @Param("today") LocalDate today);

    @Query("""
           SELECT c FROM Card c
           WHERE c.deck.owner = :owner
             AND (LOWER(c.front) LIKE LOWER(CONCAT('%', :term, '%'))
                  OR LOWER(c.back) LIKE LOWER(CONCAT('%', :term, '%')))
           """)
    Page<Card> searchByOwnerAndTerm(@Param("owner") User owner, @Param("term") String term, Pageable pageable);
}
