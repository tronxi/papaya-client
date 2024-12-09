import {Component, OnDestroy, OnInit} from '@angular/core';
import {SearchBarComponent} from '../search-bar/search-bar.component';
import {PapayaRegistryService} from '../../../services/papaya-registry.service';
import {NgForOf, NgIf} from '@angular/common';
import {RegistryCardComponent} from '../registry-card/registry-card.component';
import {Subscription} from 'rxjs';
import {SearchStateService} from '../../../state/search-state.service';
import {MatIconButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {SearchStateEvent, SearchTypes} from '../../../state/searchStateEvent';

@Component({
  selector: 'app-registry-home',
  imports: [
    SearchBarComponent,
    NgForOf,
    RegistryCardComponent,
    NgIf,
    MatIconButton,
    MatIcon
  ],
  templateUrl: './registry-home.component.html',
  styleUrl: './registry-home.component.css'
})
export class RegistryHomeComponent implements OnInit, OnDestroy {

  searchStateEvent!: SearchStateEvent;

  private readonly subscription: Subscription;

  constructor(private papayaRegistryService: PapayaRegistryService, private searchStateService: SearchStateService) {
    this.subscription = this.searchStateService.state$.subscribe(state => {
      this.searchStateEvent = state;
    })
  }

  ngOnInit(): void {
    this.papayaRegistryService.retrieveTopDownloads(1).subscribe(response => {
      this.searchStateEvent = SearchStateEvent.fromDownload(response);
    })
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  goToPage(page: number) {
    if (this.searchStateEvent.searchType === SearchTypes.Query) {
      this.papayaRegistryService.retrieveWithQuery(this.searchStateEvent.query!, page).subscribe(response => {
        this.searchStateEvent = SearchStateEvent.fromQuery(response, this.searchStateEvent.query!);
      })
    } else {
      this.papayaRegistryService.retrieveTopDownloads(page).subscribe(response => {
        this.searchStateEvent = SearchStateEvent.fromDownload(response);
      })
    }
  }
}
