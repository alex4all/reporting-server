package org.reporting.util;

import org.reporting.annotation.Sortable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;

public class SortingUtils {
    public static <T> void sort(List<T> items, String sortColumn, String sortDirection) {
        if (items == null || items.isEmpty()) {
            return;
        }

        if (sortColumn == null || sortColumn.trim().isEmpty()) {
            return;
        }

        Field sortableField = findSortableField(items.get(0).getClass(), sortColumn);
        if (sortableField == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort column: " + sortColumn);
        }

        boolean ascending = "asc".equalsIgnoreCase(sortDirection);
        if (!ascending && !"desc".equalsIgnoreCase(sortDirection)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort direction. Must be 'asc' or 'desc'");
        }

        items.sort((a, b) -> {
            try {
                Comparable<Object> valueA = (Comparable<Object>) sortableField.get(a);
                Comparable<Object> valueB = (Comparable<Object>) sortableField.get(b);
                return ascending ? valueA.compareTo(valueB) : valueB.compareTo(valueA);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing sortable field", e);
            }
        });
    }

    private static Field findSortableField(Class<?> clazz, String fieldName) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Sortable.class) && 
                field.getName().equalsIgnoreCase(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }
        return null;
    }
} 