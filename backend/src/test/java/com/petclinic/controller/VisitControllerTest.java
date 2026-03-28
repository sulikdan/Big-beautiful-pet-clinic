package com.petclinic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.dto.VisitDto;
import com.petclinic.service.VisitService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VisitController.class)
class VisitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VisitService visitService;

    private VisitDto visitDto;

    @BeforeEach
    void setUp() {
        visitDto = new VisitDto();
        visitDto.setId(1L);
        visitDto.setAnimalId(1L);
        visitDto.setAnimalName("Buddy");
        visitDto.setVisitDate(LocalDate.of(2024, 1, 10));
        visitDto.setReason("Annual check-up");
        visitDto.setWeight(30.0);
        visitDto.setHeight(65.0);
        visitDto.setAge(3.8);
        visitDto.setVetName("Dr. Evans");
        visitDto.setDiagnosis("Healthy");
    }

    @Test
    void getByAnimal_returns200WithList() throws Exception {
        when(visitService.findByAnimalId(1L)).thenReturn(List.of(visitDto));

        mockMvc.perform(get("/api/animals/1/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].reason").value("Annual check-up"))
                .andExpect(jsonPath("$[0].animalName").value("Buddy"))
                .andExpect(jsonPath("$[0].weight").value(30.0));
    }

    @Test
    void getByAnimal_noVisits_returns200WithEmptyList() throws Exception {
        when(visitService.findByAnimalId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/animals/1/visits"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(visitService.create(eq(1L), any(VisitDto.class))).thenReturn(visitDto);

        mockMvc.perform(post("/api/animals/1/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visitDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vetName").value("Dr. Evans"));
    }

    @Test
    void create_missingVisitDate_returns400() throws Exception {
        visitDto.setVisitDate(null);

        mockMvc.perform(post("/api/animals/1/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visitDto)))
                .andExpect(status().isBadRequest());

        verify(visitService, never()).create(any(), any());
    }

    @Test
    void create_animalNotFound_returns404() throws Exception {
        when(visitService.create(eq(99L), any())).thenThrow(new EntityNotFoundException("Animal not found: 99"));

        mockMvc.perform(post("/api/animals/99/visits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visitDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_found_returns200() throws Exception {
        when(visitService.findById(1L)).thenReturn(visitDto);

        mockMvc.perform(get("/api/visits/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.diagnosis").value("Healthy"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(visitService.findById(99L)).thenThrow(new EntityNotFoundException("Visit not found: 99"));

        mockMvc.perform(get("/api/visits/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_validBody_returns200() throws Exception {
        when(visitService.update(eq(1L), any(VisitDto.class))).thenReturn(visitDto);

        mockMvc.perform(put("/api/visits/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(visitDto)))
                .andExpect(status().isOk());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/visits/1"))
                .andExpect(status().isNoContent());

        verify(visitService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Visit not found: 99")).when(visitService).delete(99L);

        mockMvc.perform(delete("/api/visits/99"))
                .andExpect(status().isNotFound());
    }
}
