import {Component, inject} from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import {MatButton} from '@angular/material/button';
import {MatIcon} from '@angular/material/icon';
import {FormsModule} from '@angular/forms';
import {PapayaRegistryService} from '../../../services/papaya-registry.service';
import {SearchStateService} from '../../../state/search-state.service';

@Component({
  selector: 'app-upload-file-modal',
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatButton,
    MatIcon,
    FormsModule,
  ],
  templateUrl: './upload-file-modal.component.html',
  styleUrl: './upload-file-modal.component.css'
})
export class UploadFileModalComponent {

  selectedFile!: File;
  description!: string;

  readonly dialogRef = inject(MatDialogRef<UploadFileModalComponent>);


  constructor(private papayaRegistryService: PapayaRegistryService, private searchStateService: SearchStateService) {
  }

  upload() {
    this.papayaRegistryService.uploadFile(this.selectedFile, this.description).subscribe({
      next: _ => {
        this.papayaRegistryService.retrieveTopDownloads().subscribe(response => {
          this.searchStateService.update(response);
        });
        this.dialogRef.close();
      },
      error: _ => {
        this.dialogRef.close();
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
    }
  }

  showUploadButtonEnabled(): boolean {
    return this.selectedFile !== undefined && this.selectedFile !== null
      && this.description !== undefined && this.description !== null && this.description !== "";
  }
}
