package com.petclinic.service;

import com.petclinic.dto.VisitDto;
import com.petclinic.model.Animal;
import com.petclinic.model.Visit;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.VisitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VisitService {

    private final VisitRepository visitRepository;
    private final AnimalRepository animalRepository;

    public List<VisitDto> findByAnimalId(Long animalId) {
        return visitRepository.findByAnimalIdOrderByVisitDateDesc(animalId)
                .stream().map(this::toDto).toList();
    }

    public VisitDto findById(Long id) {
        return toDto(visitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found: " + id)));
    }

    @Transactional
    public VisitDto create(Long animalId, VisitDto dto) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + animalId));
        Visit visit = new Visit();
        visit.setAnimal(animal);
        mapToEntity(dto, visit);
        return toDto(visitRepository.save(visit));
    }

    @Transactional
    public VisitDto update(Long id, VisitDto dto) {
        Visit visit = visitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found: " + id));
        mapToEntity(dto, visit);
        return toDto(visitRepository.save(visit));
    }

    @Transactional
    public void delete(Long id) {
        visitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Visit not found: " + id));
        visitRepository.deleteById(id);
    }

    private VisitDto toDto(Visit visit) {
        VisitDto dto = new VisitDto();
        dto.setId(visit.getId());
        dto.setAnimalId(visit.getAnimal().getId());
        dto.setAnimalName(visit.getAnimal().getName());
        dto.setVisitDate(visit.getVisitDate());
        dto.setReason(visit.getReason());
        dto.setHeight(visit.getHeight());
        dto.setWeight(visit.getWeight());
        dto.setAge(visit.getAge());
        dto.setVetName(visit.getVetName());
        dto.setDiagnosis(visit.getDiagnosis());
        dto.setTreatment(visit.getTreatment());
        return dto;
    }

    private void mapToEntity(VisitDto dto, Visit visit) {
        visit.setVisitDate(dto.getVisitDate());
        visit.setReason(dto.getReason());
        visit.setHeight(dto.getHeight());
        visit.setWeight(dto.getWeight());
        visit.setAge(dto.getAge());
        visit.setVetName(dto.getVetName());
        visit.setDiagnosis(dto.getDiagnosis());
        visit.setTreatment(dto.getTreatment());
    }
}
