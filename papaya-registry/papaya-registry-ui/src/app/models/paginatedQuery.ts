import {PapayaFileRegistry} from './papayaFileRegistry';

export class PaginatedQuery {
  currentPage: number;
  totalPages: number;
  pageSize: number;
  totalItems: number;
  files: PapayaFileRegistry[];

  constructor(currentPage: number, totalPages: number, pageSize: number, totalItems: number, files: PapayaFileRegistry[]) {
    this.currentPage = currentPage;
    this.totalPages = totalPages;
    this.pageSize = pageSize;
    this.totalItems = totalItems;
    this.files = files;
  }
}
