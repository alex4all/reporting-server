package org.reporting.repository;

import org.reporting.model.ActiveUser;
import org.reporting.model.AsyncDataWrapper;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class ReportsRepository {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/yourdb";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";
    
    private final ConcurrentHashMap<String, AsyncDataWrapper<ActiveUser>> activeUsersCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final long CACHE_DURATION_MINUTES = 15;
    private static final long CLEANUP_INTERVAL_MINUTES = 5;
    private static final int DEFAULT_BATCH_SIZE = 20;

    public ReportsRepository() {
        // Schedule cache cleanup every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanupCache,
            CLEANUP_INTERVAL_MINUTES,
            CLEANUP_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
    }

    public AsyncDataWrapper<ActiveUser> getActiveUsers() {
        String cacheKey = "active_users";
        
        AsyncDataWrapper<ActiveUser> cachedData = activeUsersCache.get(cacheKey);
        if (cachedData != null && !cachedData.isExpired()) {
            return cachedData;
        }

        // Remove expired data if exists
        activeUsersCache.remove(cacheKey);
        
        // Create new data wrapper
        return activeUsersCache.computeIfAbsent(cacheKey, k -> {
            CompletableFuture<List<ActiveUser>> future = CompletableFuture.supplyAsync(() -> {
                List<ActiveUser> allUsers = new ArrayList<>();
                
                try (Connection conn = getConnection()) {
                    // First, get total count
                    int totalCount = getTotalCount(conn);
                    
                    // Create AsyncDataWrapper instance
                    AsyncDataWrapper<ActiveUser> wrapper = new AsyncDataWrapper<>(CompletableFuture.completedFuture(allUsers), CACHE_DURATION_MINUTES);
                    
                    // Load data in batches
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "SELECT id, person_number, person_source, first_name, last_name, status " +
                            "FROM active_users " +
                            "WHERE status = 'ACTIVE' " +
                            "ORDER BY id " +
                            "OFFSET ? LIMIT ?")) {
                        
                        int offset = 0;
                        while (offset < totalCount) {
                            stmt.setInt(1, offset);
                            stmt.setInt(2, DEFAULT_BATCH_SIZE);
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                List<ActiveUser> batch = new ArrayList<>();
                                while (rs.next()) {
                                    batch.add(mapResultSetToActiveUser(rs));
                                }
                                
                                // Add batch to wrapper and notify waiters
                                allUsers.addAll(batch);
                                wrapper.addBatch(batch, totalCount);
                                
                                offset += batch.size();
                                if (batch.size() < DEFAULT_BATCH_SIZE) {
                                    break; // Last batch
                                }
                            }
                        }
                        
                        wrapper.markAsFullyLoaded();
                    }
                    
                    return allUsers;
                    
                } catch (SQLException e) {
                    throw new RuntimeException("Error fetching active users", e);
                }
            });

            return new AsyncDataWrapper<>(future, CACHE_DURATION_MINUTES);
        });
    }

    private int getTotalCount(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COUNT(*) FROM active_users WHERE status = 'ACTIVE'")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private ActiveUser mapResultSetToActiveUser(ResultSet rs) throws SQLException {
        return new ActiveUser(
            rs.getLong("id"),
            rs.getString("person_number"),
            rs.getString("person_source"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("status")
        );
    }

    private void cleanupCache() {
        activeUsersCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    // Cleanup executor on application shutdown
    @jakarta.annotation.PreDestroy
    public void destroy() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(1, TimeUnit.MINUTES)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cleanupExecutor.shutdownNow();
        }
    }
} 