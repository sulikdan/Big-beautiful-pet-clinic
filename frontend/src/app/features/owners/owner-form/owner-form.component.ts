import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NgIf } from '@angular/common';
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
    RouterLink, ReactiveFormsModule, NgIf,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSnackBarModule,
  ],
  templateUrl: './owner-form.component.html',
  styleUrl: './owner-form.component.scss',
})
export class OwnerFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  ownerId?: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private ownerService: OwnerService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      email: ['', Validators.email],
      phone: [''],
      address: [''],
    });

    this.ownerId = Number(this.route.snapshot.paramMap.get('id')) || undefined;
    if (this.ownerId) {
      this.isEdit = true;
      this.ownerService.getById(this.ownerId).subscribe(o => this.form.patchValue(o));
    }
  }

  save(): void {
    if (this.form.invalid) return;
    const obs = this.isEdit
      ? this.ownerService.update(this.ownerId!, this.form.value)
      : this.ownerService.create(this.form.value);

    obs.subscribe({
      next: () => {
        this.snackBar.open(`Owner ${this.isEdit ? 'updated' : 'created'}`, 'Close', { duration: 2000 });
        this.router.navigate(['/owners']);
      },
      error: () => this.snackBar.open('Error saving owner', 'Close', { duration: 3000 }),
    });
  }
}
