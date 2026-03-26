import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Note } from '../models/note.model';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private readonly baseUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  getByAnimal(animalId: number): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.baseUrl}/animals/${animalId}/notes`);
  }

  create(animalId: number, note: Note): Observable<Note> {
    return this.http.post<Note>(`${this.baseUrl}/animals/${animalId}/notes`, note);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/notes/${id}`);
  }
}
