import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { debounceTime, switchMap } from 'rxjs';
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
    RouterLink,
    MatTableModule, MatButtonModule, MatIconModule, MatFormFieldModule,
    MatInputModule, MatCardModule, MatTooltipModule, MatSnackBarModule,
  ],
  templateUrl: './owner-list.component.html',
  styleUrl: './owner-list.component.scss',
})
export class OwnerListComponent {
  private ownerService = inject(OwnerService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  readonly displayedColumns = ['name', 'email', 'phone', 'address', 'animals', 'actions'];

  searchFilter = signal('');
  private refresh = signal(0);

  owners = toSignal(
    toObservable(computed(() => ({ q: this.searchFilter(), r: this.refresh() }))).pipe(
      debounceTime(300),
      switchMap(({ q }) => this.ownerService.getAll(q || undefined)),
    ),
    { initialValue: [] as Owner[] }
  );

  delete(owner: Owner): void {
    const ref = this.dialog.open(ConfirmationDialogComponent, {
      data: { title: 'Delete Owner', message: `Delete ${owner.firstName} ${owner.lastName}?` },
    });
    ref.afterClosed().subscribe(confirmed => {
      if (confirmed && owner.id) {
        this.ownerService.delete(owner.id).subscribe({
          next: () => {
            this.snackBar.open('Owner deleted', 'Close', { duration: 3000 });
            this.refresh.update(v => v + 1);
          },
          error: () => this.snackBar.open('Failed to delete owner', 'Close', { duration: 3000 }),
        });
      }
    });
  }
}
