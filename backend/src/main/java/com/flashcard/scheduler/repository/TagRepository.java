package com.flashcard.scheduler.repository;

import com.flashcard.scheduler.entity.Tag;
import com.flashcard.scheduler.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    List<Tag> findByOwner(User owner);
    Optional<Tag> findByOwnerAndName(User owner, String name);
}
