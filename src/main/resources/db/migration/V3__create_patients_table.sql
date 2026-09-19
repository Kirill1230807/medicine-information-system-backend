CREATE TABLE patients (
                          id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                          user_id UUID NOT NULL,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          phone_number VARCHAR(20),
                          date_of_birth DATE,
                          CONSTRAINT fk_patient_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);