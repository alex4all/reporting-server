package org.reporting.util;

import jakarta.ws.rs.BadRequestException;
import java.util.List;

public class PaginationUtils {
    public static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public static <T> List<T> paginate(List<T> items, Integer pageNumber, Integer pageSize) {
        int effectivePageSize = validateAndGetPageSize(pageSize);
        int effectivePageNumber = validateAndGetPageNumber(pageNumber);

        int fromIndex = (effectivePageNumber - 1) * effectivePageSize;
        if (fromIndex >= items.size()) {
            throw new BadRequestException("Page number " + effectivePageNumber + " is out of range");
        }

        int toIndex = Math.min(fromIndex + effectivePageSize, items.size());
        return items.subList(fromIndex, toIndex);
    }

    private static int validateAndGetPageSize(Integer pageSize) {
        if (pageSize == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (pageSize <= 0) {
            throw new BadRequestException("Page size must be greater than 0");
        }
        if (pageSize > MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size cannot be greater than " + MAX_PAGE_SIZE);
        }
        return pageSize;
    }

    private static int validateAndGetPageNumber(Integer pageNumber) {
        if (pageNumber == null) {
            return 1;
        }
        if (pageNumber <= 0) {
            throw new BadRequestException("Page number must be greater than 0");
        }
        return pageNumber;
    }
} 