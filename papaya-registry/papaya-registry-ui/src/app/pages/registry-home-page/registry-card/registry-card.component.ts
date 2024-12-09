import {Component, Input} from '@angular/core';
import {PapayaFileRegistry} from '../../../models/papayaFileRegistry';
import {
  MatCard, MatCardActions,
  MatCardContent,
  MatCardFooter,
  MatCardHeader,
  MatCardSubtitle,
  MatCardTitle
} from '@angular/material/card';
import {MatButton} from '@angular/material/button';
import {PapayaRegistryService} from '../../../services/papaya-registry.service';
import {MatIcon} from '@angular/material/icon';

@Component({
  selector: 'app-registry-card',
  imports: [
    MatCard,
    MatCardTitle,
    MatCardSubtitle,
    MatCardFooter,
    MatCardHeader,
    MatCardContent,
    MatCardActions,
    MatButton,
    MatIcon
  ],
  templateUrl: './registry-card.component.html',
  styleUrl: './registry-card.component.css'
})
export class RegistryCardComponent {

  @Input() papayaFileRegistry!: PapayaFileRegistry;

  constructor(private papayaRegistryService: PapayaRegistryService) {
  }

  downloadFile() {
    this.papayaRegistryService.download(this.papayaFileRegistry.fileId);
  }

}
