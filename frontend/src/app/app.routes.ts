import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'animals', pathMatch: 'full' },
  {
    path: 'animals',
    loadComponent: () => import('./features/animals/animal-list/animal-list.component')
      .then(m => m.AnimalListComponent),
  },
  {
    path: 'animals/new',
    loadComponent: () => import('./features/animals/animal-form/animal-form.component')
      .then(m => m.AnimalFormComponent),
  },
  {
    path: 'animals/:id',
    loadComponent: () => import('./features/animals/animal-detail/animal-detail.component')
      .then(m => m.AnimalDetailComponent),
  },
  {
    path: 'animals/:id/edit',
    loadComponent: () => import('./features/animals/animal-form/animal-form.component')
      .then(m => m.AnimalFormComponent),
  },
  {
    path: 'owners',
    loadComponent: () => import('./features/owners/owner-list/owner-list.component')
      .then(m => m.OwnerListComponent),
  },
  {
    path: 'owners/new',
    loadComponent: () => import('./features/owners/owner-form/owner-form.component')
      .then(m => m.OwnerFormComponent),
  },
  {
    path: 'owners/:id/edit',
    loadComponent: () => import('./features/owners/owner-form/owner-form.component')
      .then(m => m.OwnerFormComponent),
  },
  { path: '**', redirectTo: 'animals' },
];
