package com.petclinic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.dto.OwnerDto;
import com.petclinic.service.OwnerService;
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

@WebMvcTest(OwnerController.class)
class OwnerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OwnerService ownerService;

    private OwnerDto ownerDto;

    @BeforeEach
    void setUp() {
        ownerDto = new OwnerDto();
        ownerDto.setId(1L);
        ownerDto.setFirstName("Alice");
        ownerDto.setLastName("Johnson");
        ownerDto.setEmail("alice@example.com");
        ownerDto.setPhone("555-0101");
        ownerDto.setAddress("123 Maple Street");
        ownerDto.setAnimalCount(2);
    }

    @Test
    void getAll_noSearch_returns200WithList() throws Exception {
        when(ownerService.findAll(null)).thenReturn(List.of(ownerDto));

        mockMvc.perform(get("/api/owners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Alice"))
                .andExpect(jsonPath("$[0].animalCount").value(2));
    }

    @Test
    void getAll_withSearch_passesParamToService() throws Exception {
        when(ownerService.findAll("Alice")).thenReturn(List.of(ownerDto));

        mockMvc.perform(get("/api/owners").param("search", "Alice"))
                .andExpect(status().isOk());

        verify(ownerService).findAll("Alice");
    }

    @Test
    void getById_found_returns200() throws Exception {
        when(ownerService.findById(1L)).thenReturn(ownerDto);

        mockMvc.perform(get("/api/owners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getById_notFound_returns404WithError() throws Exception {
        when(ownerService.findById(99L)).thenThrow(new EntityNotFoundException("Owner not found: 99"));

        mockMvc.perform(get("/api/owners/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Owner not found: 99"));
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(ownerService.create(any(OwnerDto.class))).thenReturn(ownerDto);

        mockMvc.perform(post("/api/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    void create_missingFirstName_returns400() throws Exception {
        ownerDto.setFirstName(null);

        mockMvc.perform(post("/api/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerDto)))
                .andExpect(status().isBadRequest());

        verify(ownerService, never()).create(any());
    }

    @Test
    void create_invalidEmail_returns400() throws Exception {
        ownerDto.setEmail("not-an-email");

        mockMvc.perform(post("/api/owners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_validBody_returns200() throws Exception {
        when(ownerService.update(eq(1L), any(OwnerDto.class))).thenReturn(ownerDto);

        mockMvc.perform(put("/api/owners/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerDto)))
                .andExpect(status().isOk());
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(ownerService.update(eq(99L), any())).thenThrow(new EntityNotFoundException("Owner not found: 99"));

        mockMvc.perform(put("/api/owners/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/owners/1"))
                .andExpect(status().isNoContent());

        verify(ownerService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Owner not found: 99")).when(ownerService).delete(99L);

        mockMvc.perform(delete("/api/owners/99"))
                .andExpect(status().isNotFound());
    }
}
