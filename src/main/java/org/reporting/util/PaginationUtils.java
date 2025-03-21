package org.reporting.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

public class PaginationUtils {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static <T> List<T> paginate(List<T> items, Integer pageNumber, Integer pageSize) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        int effectivePageSize = pageSize != null ? pageSize : DEFAULT_PAGE_SIZE;
        int effectivePageNumber = pageNumber != null ? pageNumber : 1;

        // Validate page size
        if (effectivePageSize <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be greater than 0");
        }
        if (effectivePageSize > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size cannot be greater than " + MAX_PAGE_SIZE);
        }

        // Validate page number
        if (effectivePageNumber <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number must be greater than 0");
        }

        int totalItems = items.size();
        int totalPages = (int) Math.ceil((double) totalItems / effectivePageSize);

        if (effectivePageNumber > totalPages) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page number " + effectivePageNumber + " is out of range");
        }

        int startIndex = (effectivePageNumber - 1) * effectivePageSize;
        int endIndex = Math.min(startIndex + effectivePageSize, totalItems);

        return items.subList(startIndex, endIndex);
    }
} 