export class PapayaFileRegistry {
  fileId: string;
  fileName: string;
  path: string;
  description: string;
  downloads: number;

  constructor(fileId: string, fileName: string, path: string, description: string, downloads: number) {
    this.fileId = fileId;
    this.fileName = fileName;
    this.path = path;
    this.description = description;
    this.downloads = downloads;
  }
}
