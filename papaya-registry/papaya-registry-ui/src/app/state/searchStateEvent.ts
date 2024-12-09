import {PaginatedQuery} from '../models/paginatedQuery';

export enum SearchTypes {
  TopDownloads, Query
}

export class SearchStateEvent {
  searchType: SearchTypes;
  query?: string;
  paginatedQuery: PaginatedQuery;

  constructor(searchType: SearchTypes, paginatedQuery: PaginatedQuery, query?: string) {
    this.searchType = searchType;
    this.query = query;
    this.paginatedQuery = paginatedQuery;
  }

  static fromDownload(paginatedQuery: PaginatedQuery): SearchStateEvent {
    return new SearchStateEvent(SearchTypes.TopDownloads, paginatedQuery);
  }

  static fromQuery(paginatedQuery: PaginatedQuery, query: string): SearchStateEvent {
    return new SearchStateEvent(SearchTypes.Query, paginatedQuery, query);
  }
}
