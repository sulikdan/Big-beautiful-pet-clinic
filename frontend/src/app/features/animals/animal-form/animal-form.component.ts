import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { Animal, GENDER_LIST, SPECIES_LIST } from '../../../models/animal.model';
import { Owner } from '../../../models/owner.model';
import { AnimalService } from '../../../services/animal.service';
import { OwnerService } from '../../../services/owner.service';

@Component({
  selector: 'app-animal-form',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule, NgFor, NgIf,
    MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatDatepickerModule, MatNativeDateModule, MatSnackBarModule,
  ],
  templateUrl: './animal-form.component.html',
  styleUrl: './animal-form.component.scss',
})
export class AnimalFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  animalId?: number;
  speciesList = SPECIES_LIST;
  genderList = GENDER_LIST;
  owners: Owner[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private animalService: AnimalService,
    private ownerService: OwnerService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.ownerService.getAll().subscribe(o => this.owners = o);
    this.form = this.fb.group({
      name: ['', Validators.required],
      species: ['', Validators.required],
      breed: [''],
      dateOfBirth: [null],
      color: [''],
      gender: [''],
      ownerId: [null],
    });

    this.animalId = Number(this.route.snapshot.paramMap.get('id')) || undefined;
    if (this.animalId) {
      this.isEdit = true;
      this.animalService.getById(this.animalId).subscribe(animal => {
        this.form.patchValue({
          ...animal,
          dateOfBirth: animal.dateOfBirth ? new Date(animal.dateOfBirth) : null,
        });
      });
    }
  }

  save(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const payload: Animal = {
      ...val,
      dateOfBirth: val.dateOfBirth instanceof Date
        ? val.dateOfBirth.toISOString().split('T')[0]
        : val.dateOfBirth,
    };

    const obs = this.isEdit
      ? this.animalService.update(this.animalId!, payload)
      : this.animalService.create(payload);

    obs.subscribe({
      next: animal => {
        this.snackBar.open(`Animal ${this.isEdit ? 'updated' : 'created'}`, 'Close', { duration: 2000 });
        this.router.navigate(['/animals', animal.id]);
      },
      error: () => this.snackBar.open('Error saving animal', 'Close', { duration: 3000 }),
    });
  }
}
