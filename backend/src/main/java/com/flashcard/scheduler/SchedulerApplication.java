package com.flashcard.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the Adaptive Flashcard Scheduler application.
 *
 * This is a spaced-repetition learning platform built around a from-scratch
 * implementation of the SM-2 algorithm (see {@link com.flashcard.scheduler.service.impl.Sm2SchedulingService}).
 *
 * Architecture: layered/clean architecture -
 *   controller -> service (business logic, incl. SM-2 engine) -> repository (Spring Data JPA) -> PostgreSQL
 * Cross-cutting concerns (auth, exception handling, validation) are isolated in their own packages
 * so the domain logic (scheduling) has zero framework coupling and is unit-testable in isolation.
 */
@SpringBootApplication
@EnableScheduling
public class SchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchedulerApplication.class, args);
    }
}
