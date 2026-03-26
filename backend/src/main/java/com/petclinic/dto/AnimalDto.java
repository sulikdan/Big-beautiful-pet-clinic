package com.petclinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnimalDto {
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private String species;

    private String breed;
    private LocalDate dateOfBirth;
    private String color;
    private String gender;
    private Long ownerId;
    private String ownerName;
}
