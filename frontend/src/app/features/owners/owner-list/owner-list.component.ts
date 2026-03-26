import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { NgFor, NgIf } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { Owner } from '../../../models/owner.model';
import { OwnerService } from '../../../services/owner.service';
import { ConfirmationDialogComponent } from '../../../shared/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-owner-list',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule, NgFor, NgIf,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatCardModule, MatTooltipModule, MatSnackBarModule,
  ],
  templateUrl: './owner-list.component.html',
  styleUrl: './owner-list.component.scss',
})
export class OwnerListComponent implements OnInit {
  owners: Owner[] = [];
  displayedColumns = ['name', 'email', 'phone', 'address', 'animals', 'actions'];
  searchControl = new FormControl('');

  constructor(
    private ownerService: OwnerService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadOwners();
    this.searchControl.valueChanges.pipe(debounceTime(300), distinctUntilChanged())
      .subscribe(() => this.loadOwners());
  }

  loadOwners(): void {
    this.ownerService.getAll(this.searchControl.value ?? undefined)
      .subscribe(data => this.owners = data);
  }

  delete(owner: Owner): void {
    const ref = this.dialog.open(ConfirmationDialogComponent, {
      data: { title: 'Delete Owner', message: `Delete ${owner.firstName} ${owner.lastName}?` },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed && owner.id) {
        this.ownerService.delete(owner.id).subscribe({
          next: () => { this.snackBar.open('Owner deleted', 'Close', { duration: 3000 }); this.loadOwners(); },
          error: () => this.snackBar.open('Failed to delete owner', 'Close', { duration: 3000 }),
        });
      }
    });
  }
}
