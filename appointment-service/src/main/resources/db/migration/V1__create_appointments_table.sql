CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE appointments (
                              id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                              doctor_id UUID NOT NULL,
                              patient_id UUID NOT NULL,
                              appointment_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
                              status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
                              notes TEXT,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);