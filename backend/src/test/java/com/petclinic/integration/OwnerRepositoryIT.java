package com.petclinic.integration;

import com.petclinic.model.Owner;
import com.petclinic.repository.OwnerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private OwnerRepository ownerRepository;

    @BeforeEach
    void setUp() {
        Owner alice = new Owner();
        alice.setFirstName("Alice");
        alice.setLastName("Johnson");
        alice.setEmail("alice@example.com");
        ownerRepository.save(alice);

        Owner bob = new Owner();
        bob.setFirstName("Bob");
        bob.setLastName("Smith");
        ownerRepository.save(bob);
    }

    @AfterEach
    void tearDown() {
        ownerRepository.deleteAll();
    }

    @Test
    void findByName_matchesFirstName() {
        var result = ownerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Alice", "Alice");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findByName_matchesLastName() {
        var result = ownerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Smith", "Smith");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).isEqualTo("Smith");
    }

    @Test
    void findByName_isCaseInsensitive() {
        var result = ownerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("alice", "alice");
        assertThat(result).hasSize(1);
    }

    @Test
    void findByName_partialMatch_returnsResults() {
        var result = ownerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("Ali", "Ali");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Alice");
    }

    @Test
    void findByName_matchesBothOwners_whenTermIsCommon() {
        // Neither owner has "son" in first name, but "Johnson" has it in last name
        var result = ownerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("son", "son");
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getLastName()).isEqualTo("Johnson");
    }

    @Test
    void findByName_noMatch_returnsEmpty() {
        var result = ownerRepository
                .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase("NoOne", "NoOne");
        assertThat(result).isEmpty();
    }

    @Test
    void findAll_returnsBothOwners() {
        var all = ownerRepository.findAll();
        assertThat(all).hasSize(2);
    }
}
