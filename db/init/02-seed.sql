-- =========================================================
-- USERS
-- =========================================================

INSERT IGNORE INTO users (id, username, password_hash, role)
VALUES
    (UUID(), 'admin',
     '$2a$10$qWxdzvXM8Imqw.QOWg4fTu6js4y5j3arroJEDwnIPJ.GGDsDuAcmq',
     'ROLE_ADMIN');


-- =========================================================
-- CATEGORIES
-- =========================================================

INSERT IGNORE INTO categories (id, name)
VALUES
    (UUID(), 'Conference'),
    (UUID(), 'Workshop'),
    (UUID(), 'Webinar'),
    (UUID(), 'Meetup'),
    (UUID(), 'Festival'),
    (UUID(), 'Concert'),
    (UUID(), 'Book Talk'),
    (UUID(), 'Exhibition'),
    (UUID(), 'Networking'),
    (UUID(), 'Sports');


-- =========================================================
-- EVENTS
-- =========================================================

INSERT IGNORE INTO events
    (id, title, description, location, event_date, total_seats, available_seats)
VALUES

    -- 1. Hybrid book talk in Bucharest
    (UUID(),
     'The Future of Books',
     'A hybrid book talk with a Romanian author discussing the future of publishing, reading and literature. The event can be attended physically in Bucharest or online.',
     'Bucharest + Online',
     '2026-09-12 18:00:00',
     300,
     300),

    -- 2. Festival in Constanta
    (UUID(),
     'Black Sea Summer Festival',
     'A summer festival in Constanta featuring live music, local food, art installations and activities for the community.',
     'Constanta, Piata Ovidiu',
     '2026-09-19 16:00:00',
     5000,
     5000),

    -- 3. Concert in Brasov
    (UUID(),
     'Transylvania Live',
     'An evening concert featuring local and international artists in the heart of Brasov.',
     'Brasov, Sala Patria',
     '2026-10-03 19:00:00',
     800,
     800),

    -- 4. Technology conference in Cluj
    (UUID(),
     'Romania Tech Summit',
     'A technology conference covering artificial intelligence, cloud computing, cybersecurity and modern software development.',
     'Cluj-Napoca, BT Arena',
     '2026-10-17 09:00:00',
     2000,
     2000),

    -- 5. Art exhibition in Bucharest
    (UUID(),
     'Modern Romania',
     'An exhibition showcasing contemporary Romanian artists, photography and multimedia installations.',
     'Bucharest, National Museum of Art',
     '2026-11-07 11:00:00',
     250,
     250),

    -- 6. Sports event in Timisoara
    (UUID(),
     'Timisoara City Run',
     'A community running event with 5K, 10K and family race categories.',
     'Timisoara, Piata Victoriei',
     '2026-11-15 09:00:00',
     1500,
     1500);


-- =========================================================
-- EVENT <-> CATEGORY RELATIONSHIPS
-- =========================================================

-- 1. The Future of Books
-- Categories: Book Talk + Webinar
INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT
    e.id,
    c.id
FROM events e
CROSS JOIN categories c
WHERE e.title = 'The Future of Books'
  AND c.name IN ('Book Talk', 'Webinar');


-- 2. Black Sea Summer Festival
-- Categories: Festival + Concert
INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT
    e.id,
    c.id
FROM events e
CROSS JOIN categories c
WHERE e.title = 'Black Sea Summer Festival'
  AND c.name IN ('Festival', 'Concert');


-- 3. Transylvania Live
-- Category: Concert
INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT
    e.id,
    c.id
FROM events e
CROSS JOIN categories c
WHERE e.title = 'Transylvania Live'
  AND c.name = 'Concert';


-- 4. Romania Tech Summit
-- Category: Conference
INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT
    e.id,
    c.id
FROM events e
CROSS JOIN categories c
WHERE e.title = 'Romania Tech Summit'
  AND c.name = 'Conference';


-- 5. Modern Romania
-- Category: Exhibition
INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT
    e.id,
    c.id
FROM events e
CROSS JOIN categories c
WHERE e.title = 'Modern Romania'
  AND c.name = 'Exhibition';


-- 6. Timisoara City Run
-- Category: Sports
INSERT IGNORE INTO event_categories (event_id, category_id)
SELECT
    e.id,
    c.id
FROM events e
CROSS JOIN categories c
WHERE e.title = 'Timisoara City Run'
  AND c.name = 'Sports';