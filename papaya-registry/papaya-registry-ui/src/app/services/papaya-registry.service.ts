import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {PapayaFileRegistry} from '../models/papayaFileRegistry';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PapayaRegistryService {

  constructor(private httpClient: HttpClient) {
  }

  retrieveTopDownloads(): Observable<PapayaFileRegistry[]> {
    return this.httpClient.get<PapayaFileRegistry[]>(environment.url + "/registry/topdownloads?pageNumber=1&pageSize=10");
  }

  retrieveWithQuery(query: string): Observable<PapayaFileRegistry[]> {
    return this.httpClient.get<PapayaFileRegistry[]>(environment.url + "/registry?query=" + query);
  }

  download(fileId: string): void {
    const url = `${environment.url}/registry/${fileId}/download`;
    window.open(url, '_blank');
  }
}
