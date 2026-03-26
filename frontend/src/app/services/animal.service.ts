import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Animal } from '../models/animal.model';

@Injectable({ providedIn: 'root' })
export class AnimalService {
  private readonly baseUrl = 'http://localhost:8080/api/animals';

  constructor(private http: HttpClient) {}

  search(filters: { name?: string; species?: string; ownerId?: number }): Observable<Animal[]> {
    let params = new HttpParams();
    if (filters.name) params = params.set('name', filters.name);
    if (filters.species) params = params.set('species', filters.species);
    if (filters.ownerId) params = params.set('ownerId', filters.ownerId.toString());
    return this.http.get<Animal[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Animal> {
    return this.http.get<Animal>(`${this.baseUrl}/${id}`);
  }

  create(animal: Animal): Observable<Animal> {
    return this.http.post<Animal>(this.baseUrl, animal);
  }

  update(id: number, animal: Animal): Observable<Animal> {
    return this.http.put<Animal>(`${this.baseUrl}/${id}`, animal);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
