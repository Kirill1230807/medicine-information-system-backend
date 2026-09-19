CREATE TABLE appointments (
                              id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                              doctor_id UUID NOT NULL,
                              patient_id UUID NOT NULL,
                              appointment_datetime TIMESTAMP WITH TIME ZONE NOT NULL,
                              status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
                              notes TEXT,
                              created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
                              CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);