package org.reporting.util;

import org.reporting.annotation.Sortable;
import jakarta.ws.rs.BadRequestException;

import java.lang.reflect.Field;
import java.util.*;

public class SortingUtils {

    public static <T> void sort(List<T> items, String sortColumn, String sortDirection) {
        if (items == null || items.isEmpty() || sortColumn == null || sortColumn.trim().isEmpty()) {
            return;
        }

        Class<?> clazz = items.get(0).getClass();
        Field sortField = findSortableField(clazz, sortColumn.toLowerCase());
        
        if (sortField == null) {
            throw new BadRequestException("Invalid sort column: " + sortColumn);
        }

        String direction = Optional.ofNullable(sortDirection).orElse("asc");
        if (!direction.equalsIgnoreCase("asc") && !direction.equalsIgnoreCase("desc")) {
            throw new BadRequestException("Invalid sort direction. Must be 'asc' or 'desc'");
        }

        sortField.setAccessible(true);
        Comparator<T> comparator = createComparator(sortField);

        if (direction.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        items.sort(comparator);
    }

    private static Field findSortableField(Class<?> clazz, String sortColumn) {
        for (Field field : clazz.getDeclaredFields()) {
            Sortable sortable = field.getAnnotation(Sortable.class);
            if (sortable != null) {
                String sortableName = sortable.value().isEmpty() ? 
                    field.getName().toLowerCase() : sortable.value().toLowerCase();
                if (sortableName.equals(sortColumn)) {
                    return field;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> Comparator<T> createComparator(Field field) {
        return (item1, item2) -> {
            try {
                Object value1 = field.get(item1);
                Object value2 = field.get(item2);

                if (value1 == null && value2 == null) {
                    return 0;
                }
                if (value1 == null) {
                    return 1; // Nulls last
                }
                if (value2 == null) {
                    return -1; // Nulls last
                }

                if (value1 instanceof String) {
                    return String.CASE_INSENSITIVE_ORDER.compare((String) value1, (String) value2);
                }
                if (value1 instanceof Comparable) {
                    return ((Comparable) value1).compareTo(value2);
                }
                
                return value1.toString().compareToIgnoreCase(value2.toString());
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field during sorting", e);
            }
        };
    }
} 