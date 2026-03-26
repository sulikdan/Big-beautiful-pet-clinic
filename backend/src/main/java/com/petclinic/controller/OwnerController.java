package com.petclinic.controller;

import com.petclinic.dto.OwnerDto;
import com.petclinic.service.OwnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owners")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping
    public List<OwnerDto> getAll(@RequestParam(required = false) String search) {
        return ownerService.findAll(search);
    }

    @GetMapping("/{id}")
    public OwnerDto getById(@PathVariable Long id) {
        return ownerService.findById(id);
    }

    @PostMapping
    public ResponseEntity<OwnerDto> create(@Valid @RequestBody OwnerDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ownerService.create(dto));
    }

    @PutMapping("/{id}")
    public OwnerDto update(@PathVariable Long id, @Valid @RequestBody OwnerDto dto) {
        return ownerService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ownerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
