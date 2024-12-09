import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {PapayaFileRegistry} from '../models/papayaFileRegistry';

@Injectable({
  providedIn: 'root'
})
export class SearchStateService {

  private _searchState = new Subject<PapayaFileRegistry[]>()

  get state$() {
    return this._searchState.asObservable();
  }

  update(papayaFilesRegistry: PapayaFileRegistry[]) {
    this._searchState.next(papayaFilesRegistry);
  }
}
