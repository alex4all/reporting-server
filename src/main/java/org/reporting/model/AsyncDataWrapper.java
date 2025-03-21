package org.reporting.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.time.Instant;

public class AsyncDataWrapper<T> {
    private final List<T> loadedData;
    private final CompletableFuture<List<T>> futureData;
    private final AtomicBoolean isFullyLoaded;
    private final AtomicInteger totalItems;
    private final Instant expirationTime;
    private final ReentrantLock lock;
    private final Condition dataAvailable;

    public AsyncDataWrapper(CompletableFuture<List<T>> futureData, long expirationTimeMinutes) {
        this.loadedData = new ArrayList<>();
        this.futureData = futureData;
        this.isFullyLoaded = new AtomicBoolean(false);
        this.totalItems = new AtomicInteger(0);
        this.expirationTime = Instant.now().plusSeconds(expirationTimeMinutes * 60);
        this.lock = new ReentrantLock();
        this.dataAvailable = lock.newCondition();
    }

    public void addBatch(List<T> batch, int totalSize) {
        lock.lock();
        try {
            loadedData.addAll(batch);
            totalItems.set(totalSize);
            dataAvailable.signalAll(); // Notify waiting threads that new data is available
        } finally {
            lock.unlock();
        }
    }

    public void markAsFullyLoaded() {
        lock.lock();
        try {
            isFullyLoaded.set(true);
            dataAvailable.signalAll(); // Notify waiting threads that all data is loaded
        } finally {
            lock.unlock();
        }
    }

    public PagedResponse<T> getPage(int pageNumber, int pageSize, String sortColumn, String sortDirection) {
        int fromIndex = (pageNumber - 1) * pageSize;
        
        // Wait for minimum required data
        try {
            waitForMinimumData(fromIndex, pageSize);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for data", e);
        }

        lock.lock();
        try {
            List<T> pageData;
            int toIndex = Math.min(fromIndex + pageSize, loadedData.size());
            
            if (sortColumn != null && !sortColumn.trim().isEmpty()) {
                // Create a copy for sorting to avoid modifying the cached data
                List<T> sortedData = new ArrayList<>(loadedData);
                org.reporting.util.SortingUtils.sort(sortedData, sortColumn, sortDirection);
                pageData = sortedData.subList(fromIndex, toIndex);
            } else {
                pageData = new ArrayList<>(loadedData.subList(fromIndex, toIndex));
            }

            return new PagedResponse<>(
                pageData,
                pageNumber,
                pageSize,
                getTotalItems()
            );
        } finally {
            lock.unlock();
        }
    }

    private void waitForMinimumData(int requiredIndex, int pageSize) throws InterruptedException {
        lock.lock();
        try {
            while (!isFullyLoaded.get() && loadedData.size() < requiredIndex + pageSize) {
                // Check if the future completed with an error
                if (futureData.isDone()) {
                    try {
                        futureData.get(); // This will throw if there was an error
                    } catch (ExecutionException e) {
                        throw new RuntimeException("Error loading data", e.getCause());
                    }
                }
                // Wait for more data to become available
                dataAvailable.await(50, TimeUnit.MILLISECONDS);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isFullyLoaded() {
        return isFullyLoaded.get();
    }

    public int getTotalItems() {
        return totalItems.get();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expirationTime);
    }
} 