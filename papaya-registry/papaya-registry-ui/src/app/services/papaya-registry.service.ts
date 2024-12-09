import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';
import {PaginatedQuery} from '../models/paginatedQuery';

@Injectable({
  providedIn: 'root'
})
export class PapayaRegistryService {

  constructor(private httpClient: HttpClient) {
  }

  retrieveTopDownloads(pageNumber: number): Observable<PaginatedQuery> {
    return this.httpClient.get<PaginatedQuery>(environment.url + "/registry/topdownloads?pageNumber=" + pageNumber + "&pageSize=" + environment.pageSize);
  }

  retrieveWithQuery(query: string, pageNumber: number): Observable<PaginatedQuery> {
    return this.httpClient.get<PaginatedQuery>(environment.url + "/registry?query=" + query + "&pageNumber=" + pageNumber + "&pageSize=" + environment.pageSize);
  }

  download(fileId: string): void {
    const url = `${environment.url}/registry/${fileId}/download`;
    window.open(url, '_blank');
  }

  uploadFile(file: File, description: string) {
    const formData = new FormData();
    formData.append('papayaFile', file);
    formData.append('description', description);

    return this.httpClient.post(environment.url + "/registry", formData);
  }
}
