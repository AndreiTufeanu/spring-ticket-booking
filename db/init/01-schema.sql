CREATE TABLE IF NOT EXISTS users (
    id CHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(500) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER',
    refresh_token VARCHAR(500),
    refresh_token_expiry DATETIME
);

CREATE TABLE IF NOT EXISTS events (
    id CHAR(36) PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    location VARCHAR(300) NOT NULL,
    event_date DATETIME NOT NULL,
    total_seats INT NOT NULL CHECK (total_seats BETWEEN 1 AND 1000000),
    available_seats INT NOT NULL CHECK (available_seats BETWEEN 0 AND 1000000)
);

CREATE TABLE IF NOT EXISTS bookings (
    id CHAR(36) PRIMARY KEY,
    user_id CHAR(36) NOT NULL,
    event_id CHAR(36) NOT NULL,
    seat_number INT NOT NULL CHECK (seat_number BETWEEN 1 AND 1000000),
    UNIQUE (event_id, seat_number),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE IF NOT EXISTS categories (
    id CHAR(36) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS event_categories (
    event_id CHAR(36) NOT NULL,
    category_id CHAR(36) NOT NULL,
    PRIMARY KEY (event_id, category_id),
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);