package com.petclinic.service;

import com.petclinic.dto.AnimalDto;
import com.petclinic.model.Animal;
import com.petclinic.model.Gender;
import com.petclinic.model.Owner;
import com.petclinic.model.Species;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnimalService {

    private final AnimalRepository animalRepository;
    private final OwnerRepository ownerRepository;

    public List<AnimalDto> search(String name, String species, Long ownerId) {
        Species speciesEnum = (species != null && !species.isBlank()) ? Species.valueOf(species) : null;
        return animalRepository.search(name, speciesEnum, ownerId)
                .stream().map(this::toDto).toList();
    }

    public AnimalDto findById(Long id) {
        return toDto(animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + id)));
    }

    @Transactional
    public AnimalDto create(AnimalDto dto) {
        Animal animal = new Animal();
        mapToEntity(dto, animal);
        return toDto(animalRepository.save(animal));
    }

    @Transactional
    public AnimalDto update(Long id, AnimalDto dto) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + id));
        mapToEntity(dto, animal);
        return toDto(animalRepository.save(animal));
    }

    @Transactional
    public void delete(Long id) {
        animalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Animal not found: " + id));
        animalRepository.deleteById(id);
    }

    AnimalDto toDto(Animal animal) {
        AnimalDto dto = new AnimalDto();
        dto.setId(animal.getId());
        dto.setName(animal.getName());
        dto.setSpecies(animal.getSpecies() != null ? animal.getSpecies().name() : null);
        dto.setBreed(animal.getBreed());
        dto.setDateOfBirth(animal.getDateOfBirth());
        dto.setColor(animal.getColor());
        dto.setGender(animal.getGender() != null ? animal.getGender().name() : null);
        if (animal.getOwner() != null) {
            dto.setOwnerId(animal.getOwner().getId());
            dto.setOwnerName(animal.getOwner().getFirstName() + " " + animal.getOwner().getLastName());
        }
        return dto;
    }

    private void mapToEntity(AnimalDto dto, Animal animal) {
        animal.setName(dto.getName());
        animal.setSpecies(dto.getSpecies() != null ? Species.valueOf(dto.getSpecies()) : null);
        animal.setBreed(dto.getBreed());
        animal.setDateOfBirth(dto.getDateOfBirth());
        animal.setColor(dto.getColor());
        animal.setGender(dto.getGender() != null ? Gender.valueOf(dto.getGender()) : null);
        if (dto.getOwnerId() != null) {
            Owner owner = ownerRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + dto.getOwnerId()));
            animal.setOwner(owner);
        } else {
            animal.setOwner(null);
        }
    }
}
