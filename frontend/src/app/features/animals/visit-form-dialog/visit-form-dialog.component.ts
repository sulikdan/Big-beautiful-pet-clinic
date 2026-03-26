import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { Visit } from '../../../models/visit.model';
import { VisitService } from '../../../services/visit.service';

export interface VisitDialogData {
  visit?: Visit;
  animalId: number;
}

@Component({
  selector: 'app-visit-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatDatepickerModule, MatNativeDateModule,
  ],
  templateUrl: './visit-form-dialog.component.html',
  styleUrl: './visit-form-dialog.component.scss',
})
export class VisitFormDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private visitService = inject(VisitService);
  readonly dialogRef = inject(MatDialogRef<VisitFormDialogComponent>);
  readonly data = inject<VisitDialogData>(MAT_DIALOG_DATA);

  isEdit = signal(false);
  form!: FormGroup;

  ngOnInit(): void {
    this.isEdit.set(!!this.data.visit);
    const v = this.data.visit;
    this.form = this.fb.group({
      visitDate: [v?.visitDate ? new Date(v.visitDate) : null, Validators.required],
      reason: [v?.reason ?? ''],
      age: [v?.age ?? null],
      weight: [v?.weight ?? null],
      height: [v?.height ?? null],
      vetName: [v?.vetName ?? ''],
      diagnosis: [v?.diagnosis ?? ''],
      treatment: [v?.treatment ?? ''],
    });
  }

  save(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const visitDate = val.visitDate instanceof Date
      ? val.visitDate.toISOString().split('T')[0]
      : val.visitDate;
    const payload: Visit = { ...val, visitDate };

    const obs = this.isEdit()
      ? this.visitService.update(this.data.visit!.id!, payload)
      : this.visitService.create(this.data.animalId, payload);

    obs.subscribe({ next: () => this.dialogRef.close(true) });
  }
}
