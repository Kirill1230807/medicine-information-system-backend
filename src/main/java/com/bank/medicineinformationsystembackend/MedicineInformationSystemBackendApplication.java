package com.bank.medicineinformationsystembackend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class MedicineInformationSystemBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicineInformationSystemBackendApplication.class, args);
    }

}
