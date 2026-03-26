import { Component, computed, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { map } from 'rxjs';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { OwnerService } from '../../../services/owner.service';

@Component({
  selector: 'app-owner-form',
  standalone: true,
  imports: [
    RouterLink, ReactiveFormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSnackBarModule,
  ],
  templateUrl: './owner-form.component.html',
  styleUrl: './owner-form.component.scss',
})
export class OwnerFormComponent {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ownerService = inject(OwnerService);
  private snackBar = inject(MatSnackBar);

  private ownerId = toSignal(
    this.route.paramMap.pipe(map(p => Number(p.get('id')) || undefined))
  );

  isEdit = computed(() => !!this.ownerId());

  form = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', Validators.email],
    phone: [''],
    address: [''],
  });

  constructor() {
    const id = this.ownerId();
    if (id) {
      this.ownerService.getById(id).subscribe(o => this.form.patchValue(o));
    }
  }

  save(): void {
    if (this.form.invalid) return;
    const id = this.ownerId();
    const obs = id
      ? this.ownerService.update(id, this.form.value as any)
      : this.ownerService.create(this.form.value as any);

    obs.subscribe({
      next: () => {
        this.snackBar.open(`Owner ${this.isEdit() ? 'updated' : 'created'}`, 'Close', { duration: 2000 });
        this.router.navigate(['/owners']);
      },
      error: () => this.snackBar.open('Error saving owner', 'Close', { duration: 3000 }),
    });
  }
}
