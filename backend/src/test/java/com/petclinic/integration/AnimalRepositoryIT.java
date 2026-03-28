package com.petclinic.integration;

import com.petclinic.model.Animal;
import com.petclinic.model.Owner;
import com.petclinic.model.Species;
import com.petclinic.repository.AnimalRepository;
import com.petclinic.repository.OwnerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class AnimalRepositoryIT extends AbstractRepositoryIT {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    private Owner owner;

    @BeforeEach
    void setUp() {
        owner = new Owner();
        owner.setFirstName("Test");
        owner.setLastName("Owner");
        owner = ownerRepository.save(owner);

        Animal dog = new Animal();
        dog.setName("Rex");
        dog.setSpecies(Species.DOG);
        dog.setOwner(owner);
        animalRepository.save(dog);

        Animal cat = new Animal();
        cat.setName("Mittens");
        cat.setSpecies(Species.CAT);
        animalRepository.save(cat);

        Animal bird = new Animal();
        bird.setName("Tweety");
        bird.setSpecies(Species.BIRD);
        bird.setOwner(owner);
        animalRepository.save(bird);
    }

    @AfterEach
    void tearDown() {
        animalRepository.deleteAll();
        ownerRepository.deleteAll();
    }

    @Test
    void search_noFilters_returnsAllAnimals() {
        var result = animalRepository.search(null, null, null);
        assertThat(result).hasSize(3);
    }

    @Test
    void search_byExactName_returnsSingleMatch() {
        var result = animalRepository.search("Rex", null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rex");
    }

    @Test
    void search_byPartialName_returnsPartialMatches() {
        var result = animalRepository.search("tt", null, null);
        // "Mittens" contains "tt"
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mittens");
    }

    @Test
    void search_byName_isCaseInsensitive() {
        var result = animalRepository.search("rex", null, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rex");
    }

    @Test
    void search_bySpeciesDog_returnsOnlyDogs() {
        var result = animalRepository.search(null, Species.DOG, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecies()).isEqualTo(Species.DOG);
    }

    @Test
    void search_bySpeciesCat_returnsOnlyCats() {
        var result = animalRepository.search(null, Species.CAT, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Mittens");
    }

    @Test
    void search_byOwnerId_returnsOwnerAnimals() {
        var result = animalRepository.search(null, null, owner.getId());
        assertThat(result).hasSize(2)
                .extracting(Animal::getName)
                .containsExactlyInAnyOrder("Rex", "Tweety");
    }

    @Test
    void search_combinedNameAndSpecies_narrowsResults() {
        var result = animalRepository.search("Rex", Species.DOG, null);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rex");
    }

    @Test
    void search_combinedNameAndSpeciesMismatch_returnsEmpty() {
        // Rex is a DOG, not a CAT
        var result = animalRepository.search("Rex", Species.CAT, null);
        assertThat(result).isEmpty();
    }

    @Test
    void search_noMatch_returnsEmpty() {
        var result = animalRepository.search("NoSuchAnimal", null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void findByOwnerId_returnsOnlyOwnerAnimals() {
        var result = animalRepository.findByOwnerId(owner.getId());
        assertThat(result).hasSize(2);
    }
}
