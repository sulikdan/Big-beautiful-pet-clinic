import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { NgIf } from '@angular/common';
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
    ReactiveFormsModule, NgIf,
    MatDialogModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatDatepickerModule, MatNativeDateModule,
  ],
  templateUrl: './visit-form-dialog.component.html',
})
export class VisitFormDialogComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;

  constructor(
    private fb: FormBuilder,
    private visitService: VisitService,
    public dialogRef: MatDialogRef<VisitFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: VisitDialogData,
  ) {}

  ngOnInit(): void {
    this.isEdit = !!this.data.visit;
    const v = this.data.visit;
    this.form = this.fb.group({
      visitDate: [v?.visitDate ? new Date(v.visitDate) : null, Validators.required],
      reason: [v?.reason || ''],
      age: [v?.age || null],
      weight: [v?.weight || null],
      height: [v?.height || null],
      vetName: [v?.vetName || ''],
      diagnosis: [v?.diagnosis || ''],
      treatment: [v?.treatment || ''],
    });
  }

  save(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const visitDate = val.visitDate instanceof Date
      ? val.visitDate.toISOString().split('T')[0]
      : val.visitDate;
    const payload: Visit = { ...val, visitDate };

    const obs = this.isEdit
      ? this.visitService.update(this.data.visit!.id!, payload)
      : this.visitService.create(this.data.animalId, payload);

    obs.subscribe({ next: () => this.dialogRef.close(true), error: () => {} });
  }
}
