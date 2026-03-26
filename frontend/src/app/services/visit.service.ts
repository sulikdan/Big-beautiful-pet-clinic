import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Visit } from '../models/visit.model';

@Injectable({ providedIn: 'root' })
export class VisitService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getByAnimal(animalId: number): Observable<Visit[]> {
    return this.http.get<Visit[]>(`${this.baseUrl}/animals/${animalId}/visits`);
  }

  create(animalId: number, visit: Visit): Observable<Visit> {
    return this.http.post<Visit>(`${this.baseUrl}/animals/${animalId}/visits`, visit);
  }

  update(id: number, visit: Visit): Observable<Visit> {
    return this.http.put<Visit>(`${this.baseUrl}/visits/${id}`, visit);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/visits/${id}`);
  }
}
