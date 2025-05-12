CREATE TABLE users
(
    id          BIGSERIAL PRIMARY KEY,
    username    TEXT NOT NULL,
    email       TEXT NOT NULL,
    password    TEXT NOT NULL,
    role        TEXT NOT NULL,
    phone       TEXT,
    totp_secret TEXT
);