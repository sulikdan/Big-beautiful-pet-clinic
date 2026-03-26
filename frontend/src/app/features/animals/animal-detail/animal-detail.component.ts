import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe, NgFor, NgIf } from '@angular/common';
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
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';

import { Animal } from '../../../models/animal.model';
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
    RouterLink, ReactiveFormsModule, NgFor, NgIf, DatePipe,
    MatCardModule, MatButtonModule, MatIconModule, MatDividerModule,
    MatTableModule, MatFormFieldModule, MatInputModule, MatSnackBarModule,
    MatTooltipModule, MatTabsModule,
  ],
  templateUrl: './animal-detail.component.html',
  styleUrl: './animal-detail.component.scss',
})
export class AnimalDetailComponent implements OnInit {
  animal?: Animal;
  visits: Visit[] = [];
  notes: Note[] = [];
  visitColumns = ['visitDate', 'reason', 'age', 'weight', 'height', 'vetName', 'diagnosis', 'actions'];
  noteControl = new FormControl('', Validators.required);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private animalService: AnimalService,
    private visitService: VisitService,
    private noteService: NoteService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    forkJoin([
      this.animalService.getById(id),
      this.visitService.getByAnimal(id),
      this.noteService.getByAnimal(id),
    ]).subscribe(([animal, visits, notes]) => {
      this.animal = animal;
      this.visits = visits;
      this.notes = notes;
    });
  }

  openVisitDialog(visit?: Visit): void {
    const ref = this.dialog.open(VisitFormDialogComponent, {
      width: '600px',
      data: { visit, animalId: this.animal?.id },
    });
    ref.afterClosed().subscribe(result => {
      if (result) this.reloadVisits();
    });
  }

  deleteVisit(visit: Visit): void {
    const ref = this.dialog.open(ConfirmationDialogComponent, {
      data: { title: 'Delete Visit', message: `Delete visit from ${visit.visitDate}?` },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed && visit.id) {
        this.visitService.delete(visit.id).subscribe({
          next: () => { this.snackBar.open('Visit deleted', 'Close', { duration: 3000 }); this.reloadVisits(); },
          error: () => this.snackBar.open('Error deleting visit', 'Close', { duration: 3000 }),
        });
      }
    });
  }

  addNote(): void {
    if (this.noteControl.invalid || !this.animal?.id) return;
    this.noteService.create(this.animal.id, { content: this.noteControl.value! }).subscribe({
      next: note => {
        this.notes.unshift(note);
        this.noteControl.reset();
        this.snackBar.open('Note added', 'Close', { duration: 2000 });
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
          next: () => { this.notes = this.notes.filter(n => n.id !== note.id); },
          error: () => this.snackBar.open('Error deleting note', 'Close', { duration: 3000 }),
        });
      }
    });
  }

  private reloadVisits(): void {
    if (this.animal?.id) {
      this.visitService.getByAnimal(this.animal.id).subscribe(v => this.visits = v);
    }
  }
}
