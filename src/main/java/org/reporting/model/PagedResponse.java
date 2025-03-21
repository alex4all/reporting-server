package org.reporting.model;

import java.util.List;

public class PagedResponse<T> {
    private List<T> items;
    private int pageNumber;
    private int pageSize;
    private int totalItems;
    private int totalPages;

    public PagedResponse(List<T> items, int pageNumber, int pageSize, int totalItems) {
        this.items = items;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalItems = totalItems;
        this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }
} 