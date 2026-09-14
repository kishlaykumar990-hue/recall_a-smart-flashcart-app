-- Sample/demo data. Password for the demo user is "Password123!" (BCrypt hash below).
-- Safe to run in dev/staging; omit this migration (or truncate) before real production use.

INSERT INTO users (id, username, email, password_hash, display_name, role, current_streak, longest_streak, last_study_date)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'demo_learner',
    'demo@example.com',
    '$2b$12$5s3lBuiFqrv/O3am24B8ZOxcANpxMc22W7lTMCLn0rOuAzDOcJgyy', -- Password123! (verified BCrypt hash)
    'Demo Learner',
    'USER',
    3,
    5,
    CURRENT_DATE - INTERVAL '1 day'
);

INSERT INTO decks (id, owner_id, name, description, subject)
VALUES
    ('22222222-2222-2222-2222-222222222221', '11111111-1111-1111-1111-111111111111',
     'Spanish Verbs', 'Common irregular Spanish verb conjugations', 'Language'),
    ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111',
     'Organic Chemistry Ch.4', 'Functional groups and nomenclature', 'Chemistry');

INSERT INTO cards (id, deck_id, front, back, hint, ease_factor, repetitions, interval_days, due_date, total_reviews, lapses)
VALUES
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222221', 'ser (yo)', 'soy', 'to be (permanent)', 2.6, 2, 6, CURRENT_DATE, 3, 0),
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222221', 'estar (yo)', 'estoy', 'to be (temporary/location)', 2.5, 0, 1, CURRENT_DATE, 1, 1),
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222221', 'tener (yo)', 'tengo', 'to have', 2.7, 3, 15, CURRENT_DATE + 5, 4, 0),
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'What functional group is -OH?', 'Hydroxyl group (alcohol)', NULL, 2.5, 1, 1, CURRENT_DATE, 1, 0),
    (gen_random_uuid(), '22222222-2222-2222-2222-222222222222', 'What functional group is -COOH?', 'Carboxyl group (carboxylic acid)', NULL, 2.3, 0, 1, CURRENT_DATE, 2, 1);

INSERT INTO tags (id, owner_id, name) VALUES
    (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', 'verbs'),
    (gen_random_uuid(), '11111111-1111-1111-1111-111111111111', 'functional-groups');
