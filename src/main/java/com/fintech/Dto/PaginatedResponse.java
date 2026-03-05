package com.fintech.Dto;

import java.util.List;

public class PaginatedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final int totalElements;
    private final int totalPages;

    public PaginatedResponse(List<T> content,
                             int page,
                             int size,
                             int totalElements) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }

    public List<T> getContent() {
        return content;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }
}