package ducknetwork.domain;

import java.util.List;

public class Page<E> {
    private final List<E> content;
    private final int totalPages;
    private final long totalElements;
    private final int currentPage;
    private final int pageSize;

    public Page(List<E> content, long totalElements, int currentPage, int pageSize) {
        this.content = content;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.currentPage = currentPage;
    }

    public List<E> getContent() { return content; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public int getCurrentPage() { return currentPage; }
    public int getPageSize() { return pageSize; }

    public boolean hasNext() { return currentPage < totalPages; }
    public boolean hasPrevious() { return currentPage > 1; }
}