import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Owner } from '../models/owner.model';

@Injectable({ providedIn: 'root' })
export class OwnerService {
  private readonly baseUrl = 'http://localhost:8080/api/owners';

  constructor(private http: HttpClient) {}

  getAll(search?: string): Observable<Owner[]> {
    const params = search ? new HttpParams().set('search', search) : undefined;
    return this.http.get<Owner[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<Owner> {
    return this.http.get<Owner>(`${this.baseUrl}/${id}`);
  }

  create(owner: Owner): Observable<Owner> {
    return this.http.post<Owner>(this.baseUrl, owner);
  }

  update(id: number, owner: Owner): Observable<Owner> {
    return this.http.put<Owner>(`${this.baseUrl}/${id}`, owner);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
