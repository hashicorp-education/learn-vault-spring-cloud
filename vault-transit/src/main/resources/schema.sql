set time zone 'UTC';
create extension if not exists pgcrypto;

CREATE TABLE if not exists payments
(
    id              VARCHAR(255) PRIMARY KEY NOT NULL,
    name            VARCHAR(255)             NOT NULL,
    cc_info         VARCHAR(255)             NOT NULL,
    created_at      TIMESTAMP                NOT NULL
);