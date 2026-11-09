CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
                       id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       role VARCHAR(50) NOT NULL,
                       external_id VARCHAR(255) UNIQUE
);

CREATE INDEX idx_users_external_id ON users(external_id);

CREATE TABLE doctors (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         user_id UUID NOT NULL,
                         first_name VARCHAR(100) NOT NULL,
                         last_name VARCHAR(100) NOT NULL,
                         specialization VARCHAR(100) NOT NULL,
                         cabinet_number VARCHAR(20),
                         CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE patients (
                          id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                          user_id UUID NOT NULL,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          phone_number VARCHAR(20),
                          date_of_birth DATE,
                          CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);