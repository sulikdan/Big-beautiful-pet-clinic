package com.petclinic.repository;

import com.petclinic.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByAnimalIdOrderByCreatedAtDesc(Long animalId);
}
