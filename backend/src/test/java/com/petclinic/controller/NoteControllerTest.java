package com.petclinic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petclinic.dto.NoteDto;
import com.petclinic.service.NoteService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NoteController.class)
class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NoteService noteService;

    private NoteDto noteDto;

    @BeforeEach
    void setUp() {
        noteDto = new NoteDto();
        noteDto.setId(1L);
        noteDto.setAnimalId(1L);
        noteDto.setContent("Buddy loves fetch.");
        noteDto.setCreatedAt(LocalDateTime.of(2024, 1, 10, 10, 30));
    }

    @Test
    void getByAnimal_returns200WithList() throws Exception {
        when(noteService.findByAnimalId(1L)).thenReturn(List.of(noteDto));

        mockMvc.perform(get("/api/animals/1/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Buddy loves fetch."))
                .andExpect(jsonPath("$[0].animalId").value(1));
    }

    @Test
    void getByAnimal_noNotes_returns200WithEmptyList() throws Exception {
        when(noteService.findByAnimalId(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/animals/1/notes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void create_validBody_returns201() throws Exception {
        when(noteService.create(eq(1L), any(NoteDto.class))).thenReturn(noteDto);

        mockMvc.perform(post("/api/animals/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Buddy loves fetch."));
    }

    @Test
    void create_blankContent_returns400() throws Exception {
        noteDto.setContent("  ");

        mockMvc.perform(post("/api/animals/1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isBadRequest());

        verify(noteService, never()).create(any(), any());
    }

    @Test
    void create_animalNotFound_returns404() throws Exception {
        when(noteService.create(eq(99L), any())).thenThrow(new EntityNotFoundException("Animal not found: 99"));

        mockMvc.perform(post("/api/animals/99/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noteDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mockMvc.perform(delete("/api/notes/1"))
                .andExpect(status().isNoContent());

        verify(noteService).delete(1L);
    }

    @Test
    void delete_notFound_returns404() throws Exception {
        doThrow(new EntityNotFoundException("Note not found: 99")).when(noteService).delete(99L);

        mockMvc.perform(delete("/api/notes/99"))
                .andExpect(status().isNotFound());
    }
}
