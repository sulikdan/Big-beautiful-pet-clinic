import {Component, computed, inject, Signal, signal} from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { map } from 'rxjs';
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
import { AnimalService } from '../../../services/animal.service';
import { OwnerService } from '../../../services/owner.service';

@Component({
  selector: 'app-animal-form',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatDatepickerModule, MatNativeDateModule, MatSnackBarModule,
  ],
  templateUrl: './animal-form.component.html',
  styleUrl: './animal-form.component.scss',
})
export class AnimalFormComponent {
  get animalId(): Signal<number | undefined> {
    return this._animalId;
  }
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private animalService = inject(AnimalService);
  private ownerService = inject(OwnerService);
  private snackBar = inject(MatSnackBar);

  readonly speciesList = SPECIES_LIST;
  readonly genderList = GENDER_LIST;

  owners = toSignal(this.ownerService.getAll(), { initialValue: [] });

  private _animalId = toSignal(
    this.route.paramMap.pipe(map(p => Number(p.get('id')) || undefined))
  );

  isEdit = computed(() => !!this._animalId());

  form = this.fb.group({
    name: ['', Validators.required],
    species: ['', Validators.required],
    breed: [''],
    dateOfBirth: [null as Date | null],
    color: [''],
    gender: [''],
    ownerId: [null as number | null],
  });

  constructor() {
    // Patch form when editing an existing animal
    const id = this._animalId();
    if (id) {
      this.animalService.getById(id).subscribe(animal => {
        this.form.patchValue({
          ...animal,
          dateOfBirth: animal.dateOfBirth ? new Date(animal.dateOfBirth) : null,
          ownerId: animal.ownerId ?? null,
        } as any);
      });
    }
  }

  save(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const payload: Animal = {
      ...val as any,
      dateOfBirth: val.dateOfBirth instanceof Date
        ? val.dateOfBirth.toISOString().split('T')[0]
        : val.dateOfBirth ?? undefined,
    };

    const id = this._animalId();
    const obs = id
      ? this.animalService.update(id, payload)
      : this.animalService.create(payload);

    obs.subscribe({
      next: animal => {
        this.snackBar.open(`Animal ${this.isEdit() ? 'updated' : 'created'}`, 'Close', { duration: 2000 });
        this.router.navigate(['/animals', animal.id]);
      },
      error: () => this.snackBar.open('Error saving animal', 'Close', { duration: 3000 }),
    });
  }
}
