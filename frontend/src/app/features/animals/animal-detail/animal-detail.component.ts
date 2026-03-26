import { Component, computed, effect, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { filter, map, switchMap } from 'rxjs';
import { DatePipe } from '@angular/common';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

import { Visit } from '../../../models/visit.model';
import { Note } from '../../../models/note.model';
import { AnimalService } from '../../../services/animal.service';
import { VisitService } from '../../../services/visit.service';
import { NoteService } from '../../../services/note.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';
import { VisitFormDialogComponent } from '../visit-form-dialog/visit-form-dialog.component';

@Component({
  selector: 'app-animal-detail',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule, DatePipe,
    MatCardModule, MatButtonModule, MatIconModule, MatDividerModule,
    MatTableModule, MatFormFieldModule, MatInputModule, MatSnackBarModule,
    MatTooltipModule,
  ],
  templateUrl: './animal-detail.component.html',
  styleUrl: './animal-detail.component.scss',
})
export class AnimalDetailComponent {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private animalService = inject(AnimalService);
  private visitService = inject(VisitService);
  private noteService = inject(NoteService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  readonly visitColumns = ['visitDate', 'reason', 'age', 'weight', 'height', 'vetName', 'diagnosis', 'actions'];

  private animalId = toSignal(
    this.route.paramMap.pipe(map(p => Number(p.get('id'))))
  );

  animal = toSignal(
    toObservable(this.animalId).pipe(
      filter(Boolean),
      switchMap(id => this.animalService.getById(id))
    )
  );

  // Refresh triggers for imperative reloads
  private visitRefresh = signal(0);
  private noteRefresh = signal(0);

  visits = toSignal(
    toObservable(computed(() => ({ id: this.animalId(), r: this.visitRefresh() }))).pipe(
      filter(({ id }) => !!id),
      switchMap(({ id }) => this.visitService.getByAnimal(id!))
    ),
    { initialValue: [] as Visit[] }
  );

  notes = toSignal(
    toObservable(computed(() => ({ id: this.animalId(), r: this.noteRefresh() }))).pipe(
      filter(({ id }) => !!id),
      switchMap(({ id }) => this.noteService.getByAnimal(id!))
    ),
    { initialValue: [] as Note[] }
  );

  noteControl = new FormControl('', Validators.required);

  openVisitDialog(visit?: Visit): void {
    const ref = this.dialog.open(VisitFormDialogComponent, {
      width: '600px',
      data: { visit, animalId: this.animalId() },
    });
    ref.afterClosed().subscribe(saved => {
      if (saved) this.visitRefresh.update(v => v + 1);
    });
  }

  deleteVisit(visit: Visit): void {
    const ref = this.dialog.open(ConfirmationDialogComponent, {
      data: { title: 'Delete Visit', message: `Delete visit from ${visit.visitDate}?` },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed && visit.id) {
        this.visitService.delete(visit.id).subscribe({
          next: () => {
            this.snackBar.open('Visit deleted', 'Close', { duration: 3000 });
            this.visitRefresh.update(v => v + 1);
          },
          error: () => this.snackBar.open('Error deleting visit', 'Close', { duration: 3000 }),
        });
      }
    });
  }

  addNote(): void {
    if (this.noteControl.invalid) return;
    const id = this.animalId();
    if (!id) return;
    this.noteService.create(id, { content: this.noteControl.value! }).subscribe({
      next: () => {
        this.noteControl.reset();
        this.snackBar.open('Note added', 'Close', { duration: 2000 });
        this.noteRefresh.update(v => v + 1);
      },
      error: () => this.snackBar.open('Error adding note', 'Close', { duration: 3000 }),
    });
  }

  deleteNote(note: Note): void {
    const ref = this.dialog.open(ConfirmationDialogComponent, {
      data: { title: 'Delete Note', message: 'Delete this note?' },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed && note.id) {
        this.noteService.delete(note.id).subscribe({
          next: () => this.noteRefresh.update(v => v + 1),
          error: () => this.snackBar.open('Error deleting note', 'Close', { duration: 3000 }),
        });
      }
    });
  }
}
