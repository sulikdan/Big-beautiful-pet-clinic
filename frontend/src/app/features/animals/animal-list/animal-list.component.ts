import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, switchMap } from 'rxjs';
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

import { Animal, SPECIES_LIST } from '../../../models/animal.model';
import { AnimalService } from '../../../services/animal.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-animal-list',
  standalone: true,
  imports: [
    RouterLink,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatCardModule, MatTooltipModule, MatSnackBarModule,
  ],
  templateUrl: './animal-list.component.html',
  styleUrl: './animal-list.component.scss',
})
export class AnimalListComponent {
  private animalService = inject(AnimalService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  readonly speciesList = SPECIES_LIST;
  readonly displayedColumns = ['name', 'species', 'breed', 'gender', 'owner', 'actions'];

  nameFilter = signal('');
  speciesFilter = signal('');

  private refresh = signal(0);

  private filters = toObservable(
    computed(() => ({
      name: this.nameFilter(),
      species: this.speciesFilter(),
      _r: this.refresh(),
    }))
  );

  animals = toSignal(
    this.filters.pipe(
      debounceTime(300),
      switchMap(f => this.animalService.search({ name: f.name, species: f.species })),
    ),
    { initialValue: [] as Animal[] }
  );

  clearFilters(): void {
    this.nameFilter.set('');
    this.speciesFilter.set('');
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
            this.refresh.update(v => v + 1);
          },
          error: () => this.snackBar.open('Failed to delete animal', 'Close', { duration: 3000 }),
        });
      }
    });
  }
}
