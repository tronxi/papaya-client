import {Component} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {PapayaRegistryService} from '../../../services/papaya-registry.service';
import {debounceTime, Subject} from 'rxjs';
import {SearchStateService} from '../../../state/search-state.service';

@Component({
  selector: 'app-search-bar',
  imports: [
    MatIcon
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css'
})
export class SearchBarComponent {

  private inputSubject = new Subject<string>();

  constructor(private papayaRegistryService: PapayaRegistryService, private searchStateService: SearchStateService) {
    this.inputSubject.pipe(debounceTime(700)).subscribe((value) => {
      this.onSearch(value);
    });
  }

  onInputChange($event: Event) {
    const input = $event.target as HTMLInputElement;
    this.inputSubject.next(input.value);
  }

  onSearch(query: string): void {
    if (query.length >= 4) {
      this.papayaRegistryService.retrieveWithQuery(query).subscribe(response => {
        this.searchStateService.update(response);
      });
    } else if (query.length === 0) {
      this.papayaRegistryService.retrieveTopDownloads().subscribe(response => {
        this.searchStateService.update(response);
      })
    }
  }
}
