import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {SearchStateEvent} from './searchStateEvent';

@Injectable({
  providedIn: 'root'
})
export class SearchStateService {

  private _searchState = new Subject<SearchStateEvent>()

  get state$() {
    return this._searchState.asObservable();
  }

  update(searchStateEvent: SearchStateEvent) {
    this._searchState.next(searchStateEvent);
  }
}
