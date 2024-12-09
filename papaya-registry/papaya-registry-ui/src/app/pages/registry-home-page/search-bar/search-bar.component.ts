import {Component, inject} from '@angular/core';
import {MatIcon} from '@angular/material/icon';
import {PapayaRegistryService} from '../../../services/papaya-registry.service';
import {debounceTime, Subject} from 'rxjs';
import {SearchStateService} from '../../../state/search-state.service';
import {MatButton} from '@angular/material/button';
import {MatDialog} from '@angular/material/dialog';
import {UploadFileModalComponent} from '../upload-file-modal/upload-file-modal.component';
import {SearchStateEvent} from '../../../state/searchStateEvent';

@Component({
  selector: 'app-search-bar',
  imports: [
    MatIcon,
    MatButton
  ],
  templateUrl: './search-bar.component.html',
  styleUrl: './search-bar.component.css'
})
export class SearchBarComponent {

  readonly dialog = inject(MatDialog);

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
    if (query.length >= 3) {
      this.papayaRegistryService.retrieveWithQuery(query, 1).subscribe(response => {
        this.searchStateService.update(SearchStateEvent.fromQuery(response, query));
      });
    } else if (query.length === 0) {
      this.papayaRegistryService.retrieveTopDownloads(1).subscribe(response => {
        this.searchStateService.update(SearchStateEvent.fromDownload(response));
      })
    }
  }

  uploadFile() {
    this.dialog.open(UploadFileModalComponent, {});
  }
}
