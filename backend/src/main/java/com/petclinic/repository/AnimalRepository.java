package com.petclinic.repository;

import com.petclinic.model.Animal;
import com.petclinic.model.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnimalRepository extends JpaRepository<Animal, Long> {

    @Query("SELECT a FROM Animal a LEFT JOIN FETCH a.owner WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:species IS NULL OR a.species = :species) AND " +
           "(:ownerId IS NULL OR a.owner.id = :ownerId)")
    List<Animal> search(@Param("name") String name,
                        @Param("species") Species species,
                        @Param("ownerId") Long ownerId);

    List<Animal> findByOwnerId(Long ownerId);
}
