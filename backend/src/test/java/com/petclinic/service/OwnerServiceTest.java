package com.petclinic.service;

import com.petclinic.dto.OwnerDto;
import com.petclinic.model.Owner;
import com.petclinic.repository.OwnerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @InjectMocks
    private OwnerService ownerService;

    private Owner owner;
    private OwnerDto dto;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setId(1L);
        owner.setFirstName("Alice");
        owner.setLastName("Johnson");
        owner.setEmail("alice@example.com");
        owner.setPhone("555-0101");
        owner.setAddress("123 Maple Street");

        dto = new OwnerDto();
        dto.setFirstName("Alice");
        dto.setLastName("Johnson");
        dto.setEmail("alice@example.com");
        dto.setPhone("555-0101");
        dto.setAddress("123 Maple Street");
    }

    @Test
    void findAll_nullSearch_returnsAll() {
        when(ownerRepository.findAll()).thenReturn(List.of(owner));

        var result = ownerService.findAll(null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
        verify(ownerRepository).findAll();
        verify(ownerRepository, never())
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(any(), any());
    }

    @Test
    void findAll_blankSearch_returnsAll() {
        when(ownerRepository.findAll()).thenReturn(List.of(owner));

        var result = ownerService.findAll("   ");

        assertThat(result).hasSize(1);
        verify(ownerRepository).findAll();
    }

    @Test
    void findAll_withSearch_delegatesToSearchQuery() {
        when(ownerRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Alice", "Alice"))
                .thenReturn(List.of(owner));

        var result = ownerService.findAll("Alice");

        assertThat(result).hasSize(1);
        verify(ownerRepository)
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Alice", "Alice");
        verify(ownerRepository, never()).findAll();
    }

    @Test
    void findById_found_returnsDto() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));

        var result = ownerService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("alice@example.com");
        assertThat(result.getAnimalCount()).isZero();
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_validDto_savesAndReturnsDto() {
        when(ownerRepository.save(any(Owner.class))).thenReturn(owner);

        var result = ownerService.create(dto);

        assertThat(result.getFirstName()).isEqualTo("Alice");
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void update_found_updatesAllFields() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(ownerRepository.save(owner)).thenReturn(owner);
        dto.setFirstName("Updated");
        dto.setLastName("Name");

        ownerService.update(1L, dto);

        assertThat(owner.getFirstName()).isEqualTo("Updated");
        assertThat(owner.getLastName()).isEqualTo("Name");
        verify(ownerRepository).save(owner);
    }

    @Test
    void update_notFound_throwsEntityNotFoundException() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.update(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
        verify(ownerRepository, never()).save(any());
    }

    @Test
    void delete_found_deletesById() {
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(owner));

        ownerService.delete(1L);

        verify(ownerRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsEntityNotFoundException() {
        when(ownerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ownerService.delete(99L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(ownerRepository, never()).deleteById(any());
    }
}
