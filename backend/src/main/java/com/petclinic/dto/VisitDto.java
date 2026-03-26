package com.petclinic.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class VisitDto {
    private Long id;
    private Long animalId;
    private String animalName;

    @NotNull
    private LocalDate visitDate;

    private String reason;
    private Double height;
    private Double weight;
    private Double age;
    private String vetName;
    private String diagnosis;
    private String treatment;
}
