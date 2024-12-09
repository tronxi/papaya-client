package dev.tronxi.papayaregistryback.models;

import java.util.List;

public class PaginatedQuery {
    private int currentPage;
    private long totalPages;
    private int pageSize;
    private long totalItems;
    private List<PapayaFileRegistry> files;

    public PaginatedQuery(int currentPage, long totalPages, int pageSize, long totalItems, List<PapayaFileRegistry> files) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.files = files;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(long totalPages) {
        this.totalPages = totalPages;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public List<PapayaFileRegistry> getFiles() {
        return files;
    }

    public void setFiles(List<PapayaFileRegistry> files) {
        this.files = files;
    }
}
