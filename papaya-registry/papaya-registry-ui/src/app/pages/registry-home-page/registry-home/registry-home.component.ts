import {Component, OnDestroy, OnInit} from '@angular/core';
import {SearchBarComponent} from '../search-bar/search-bar.component';
import {PapayaRegistryService} from '../../../services/papaya-registry.service';
import {PapayaFileRegistry} from '../../../models/papayaFileRegistry';
import {NgForOf} from '@angular/common';
import {RegistryCardComponent} from '../registry-card/registry-card.component';
import {Subscription} from 'rxjs';
import {SearchStateService} from '../../../state/search-state.service';

@Component({
  selector: 'app-registry-home',
  imports: [
    SearchBarComponent,
    NgForOf,
    RegistryCardComponent
  ],
  templateUrl: './registry-home.component.html',
  styleUrl: './registry-home.component.css'
})
export class RegistryHomeComponent implements OnInit, OnDestroy {

  papayaFilesRegister: PapayaFileRegistry[] = [];

  private readonly subscription: Subscription;

  constructor(private papayaRegistryService: PapayaRegistryService, private searchStateService: SearchStateService) {
    this.subscription = this.searchStateService.state$.subscribe(state => {
      this.papayaFilesRegister = state;
    })
  }

  ngOnInit(): void {
    this.papayaRegistryService.retrieveTopDownloads().subscribe(response => {
      this.papayaFilesRegister = response;
    })
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

}
