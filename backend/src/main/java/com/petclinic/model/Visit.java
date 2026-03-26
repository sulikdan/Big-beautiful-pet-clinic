package com.petclinic.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "visits")
@Getter
@Setter
@NoArgsConstructor
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "animal_id", nullable = false)
    private Animal animal;

    @NotNull
    private LocalDate visitDate;

    private String reason;

    /** Height in centimetres */
    private Double height;

    /** Weight in kilograms */
    private Double weight;

    /** Age in years at time of visit */
    private Double age;

    private String vetName;

    private String diagnosis;

    @Column(length = 2000)
    private String treatment;
}
