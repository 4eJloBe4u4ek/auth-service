CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    username   TEXT NOT NULL UNIQUE,
    email      TEXT NOT NULL,
    password   TEXT NOT NULL,
    role       TEXT NOT NULL
);