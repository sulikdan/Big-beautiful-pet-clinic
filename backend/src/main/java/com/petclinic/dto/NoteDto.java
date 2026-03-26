package com.petclinic.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NoteDto {
    private Long id;
    private Long animalId;

    @NotBlank
    private String content;

    private LocalDateTime createdAt;
}
