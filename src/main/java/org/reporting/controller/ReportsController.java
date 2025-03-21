package org.reporting.controller;

import org.reporting.model.ActiveUser;
import org.reporting.model.AsyncDataWrapper;
import org.reporting.model.PagedResponse;
import org.reporting.repository.ReportsRepository;
import org.reporting.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
public class ReportsController {

    private final ReportsRepository reportsRepository;

    @Autowired
    public ReportsController(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    @GetMapping("/active-users")
    public ResponseEntity<PagedResponse<ActiveUser>> getActiveUsers(
            @RequestParam(value = "sort_column", required = false) String sortColumn,
            @RequestParam(value = "sort_direction", required = false) String sortDirection,
            @RequestParam(value = "page_number", required = false) Integer pageNumber,
            @RequestParam(value = "page_size", required = false) Integer pageSize) {
        
        // Get the async data wrapper (might be cached)
        AsyncDataWrapper<ActiveUser> dataWrapper = reportsRepository.getActiveUsers();

        // Get effective pagination parameters
        int effectivePageNumber = pageNumber != null ? pageNumber : 1;
        int effectivePageSize = pageSize != null ? pageSize : PaginationUtils.DEFAULT_PAGE_SIZE;

        // Get the page (this will wait for the minimum required data)
        PagedResponse<ActiveUser> response = dataWrapper.getPage(
            effectivePageNumber,
            effectivePageSize,
            sortColumn,
            sortDirection
        );

        return ResponseEntity.ok(response);
    }
} 