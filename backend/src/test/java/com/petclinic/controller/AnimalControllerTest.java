package com.petclinic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.dto.AnimalDto;
import com.petclinic.service.AnimalService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimalController.class)
class AnimalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnimalService animalService;

    private AnimalDto animalDto;

    @BeforeEach
    void setUp() {
        animalDto = new AnimalDto();
        animalDto.setId(1L);
        animalDto.setName("Buddy");
        animalDto.setSpecies("DOG");
        animalDto.setBreed("Golden Retriever");
        animalDto.setOwnerId(1L);
        animalDto.setOwnerName("Alice Johnson");
    }

    @Test
    void search_noParams_returns200WithList() throws Exception {
        when(animalService.search(null, null, null)).thenReturn(List.of(animalDto));

        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Buddy"))
                .andExpect(jsonPath("$[0].species").value("DOG"));
    }

    @Test
    void search_withNameAndSpecies_passesParamsToService() throws Exception {
        when(animalService.search("Buddy", "DOG", null)).thenReturn(List.of(animalDto));

        mockMvc.perform(get("/api/animals")
                        .param("name", "Buddy")
                        .param("species", "DOG"))
                .andExpect(status().isOk());

        verify(animalService).search("Buddy", "DOG", null);
    }

    @Test
    void search_emptyResult_returns200WithEmptyArray() throws Exception {
        when(animalService.search(any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/animals"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getById_found_returns200WithBody() throws Exception {
        when(animalService.findById(1L)).thenReturn(animalDto);

        mockMvc.perform(get("/api/animals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.ownerName").value("Alice Johnson"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(animalService.findById(99L)).thenThrow(new EntityNotFoundException("Animal not found: 99"));

        mockMvc.perform(get("/api/animals/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Animal not found: 99"));
    }

    @Test
    void create_validBody_returns201WithDto() throws Exception {
        when(animalService.create(any(AnimalDto.class))).thenReturn(animalDto);

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void create_missingName_returns400() throws Exception {
        animalDto.setName(null);

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalDto)))
                .andExpect(status().isBadRequest());

        verify(animalService, never()).create(any());
    }

    @Test
    void create_missingSpecies_returns400() throws Exception {
        animalDto.setSpecies(null);

        mockMvc.perform(post("/api/animals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validBody_returns200() throws Exception {
        when(animalService.update(eq(1L), any(AnimalDto.class))).thenReturn(animalDto);

        mockMvc.perform(put("/api/animals/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(animalService.update(eq(99L), any())).thenThrow(new EntityNotFoundException("Animal not found: 99"));

        mockMvc.perform(put("/api/animals/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(animalDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/animals/1"))
                .andExpect(status().isNoContent());

        verify(animalService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Animal not found: 99")).when(animalService).delete(99L);

        mockMvc.perform(delete("/api/animals/99"))
                .andExpect(status().isNotFound());
    }
}
