package com.petclinic.service;

import com.petclinic.dto.OwnerDto;
import com.petclinic.model.Owner;
import com.petclinic.repository.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OwnerService {

    private final OwnerRepository ownerRepository;

    public List<OwnerDto> findAll(String search) {
        List<Owner> owners = (search != null && !search.isBlank())
                ? ownerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(search, search)
                : ownerRepository.findAll();
        return owners.stream().map(this::toDto).toList();
    }

    public OwnerDto findById(Long id) {
        return toDto(ownerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + id)));
    }

    @Transactional
    public OwnerDto create(OwnerDto dto) {
        Owner owner = new Owner();
        mapToEntity(dto, owner);
        return toDto(ownerRepository.save(owner));
    }

    @Transactional
    public OwnerDto update(Long id, OwnerDto dto) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + id));
        mapToEntity(dto, owner);
        return toDto(ownerRepository.save(owner));
    }

    @Transactional
    public void delete(Long id) {
        ownerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Owner not found: " + id));
        ownerRepository.deleteById(id);
    }

    private OwnerDto toDto(Owner owner) {
        OwnerDto dto = new OwnerDto();
        dto.setId(owner.getId());
        dto.setFirstName(owner.getFirstName());
        dto.setLastName(owner.getLastName());
        dto.setEmail(owner.getEmail());
        dto.setPhone(owner.getPhone());
        dto.setAddress(owner.getAddress());
        dto.setAnimalCount(owner.getAnimals().size());
        return dto;
    }

    private void mapToEntity(OwnerDto dto, Owner owner) {
        owner.setFirstName(dto.getFirstName());
        owner.setLastName(dto.getLastName());
        owner.setEmail(dto.getEmail());
        owner.setPhone(dto.getPhone());
        owner.setAddress(dto.getAddress());
    }
}
