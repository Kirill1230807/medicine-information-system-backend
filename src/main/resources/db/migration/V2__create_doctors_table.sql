CREATE TABLE doctors (
                         id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
                         user_id UUID NOT NULL,
                         first_name VARCHAR(100) NOT NULL,
                         last_name VARCHAR(100) NOT NULL,
                         specialization VARCHAR(100) NOT NULL,
                         cabinet_number VARCHAR(20),
                         CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);