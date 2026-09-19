CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       role VARCHAR(50) NOT NULL,
                       external_id VARCHAR(255) UNIQUE
);

CREATE INDEX idx_users_external_id ON users(external_id);