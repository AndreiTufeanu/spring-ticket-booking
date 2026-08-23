INSERT IGNORE INTO users (id, username, password_hash, role)
VALUES (UUID(), 'admin', '$2a$10$qWxdzvXM8Imqw.QOWg4fTu6js4y5j3arroJEDwnIPJ.GGDsDuAcmq', 'ROLE_ADMIN');

INSERT IGNORE INTO categories (id, name)
VALUES
    (UUID(), 'Conference'),
    (UUID(), 'Workshop'),
    (UUID(), 'Webinar'),
    (UUID(), 'Meetup');