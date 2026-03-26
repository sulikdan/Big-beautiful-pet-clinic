import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { NgFor, NgIf } from '@angular/common';

import { Animal, SPECIES_LIST } from '../../../models/animal.model';
import { AnimalService } from '../../../services/animal.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-animal-list',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule, NgFor, NgIf,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatCardModule, MatTooltipModule, MatSnackBarModule,
  ],
  templateUrl: './animal-list.component.html',
  styleUrl: './animal-list.component.scss',
})
export class AnimalListComponent implements OnInit {
  animals: Animal[] = [];
  speciesList = SPECIES_LIST;
  displayedColumns = ['name', 'species', 'breed', 'gender', 'owner', 'actions'];

  nameControl = new FormControl('');
  speciesControl = new FormControl('');

  constructor(
    private animalService: AnimalService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.loadAnimals();
    this.nameControl.valueChanges.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.loadAnimals());
    this.speciesControl.valueChanges.subscribe(() => this.loadAnimals());
  }

  loadAnimals(): void {
    this.animalService.search({
      name: this.nameControl.value ?? undefined,
      species: this.speciesControl.value ?? undefined,
    }).subscribe(data => this.animals = data);
  }

  clearFilters(): void {
    this.nameControl.setValue('');
    this.speciesControl.setValue('');
  }

  delete(animal: Animal): void {
    const ref = this.dialog.open(ConfirmationDialogComponent, {
      data: { title: 'Delete Animal', message: `Delete ${animal.name}?` },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed && animal.id) {
        this.animalService.delete(animal.id).subscribe({
          next: () => {
            this.snackBar.open('Animal deleted', 'Close', { duration: 3000 });
            this.loadAnimals();
          },
          error: () => this.snackBar.open('Failed to delete animal', 'Close', { duration: 3000 }),
        });
      }
    });
  }
}
