package org.reporting.controller;

import org.reporting.model.ActiveUser;
import org.reporting.model.AsyncDataWrapper;
import org.reporting.model.PagedResponse;
import org.reporting.repository.ReportsRepository;
import org.reporting.util.PaginationUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/reports")
@Produces(MediaType.APPLICATION_JSON)
public class ReportsController {

    @Inject
    private ReportsRepository reportsRepository;

    @GET
    @Path("/active-users")
    public Response getActiveUsers(
            @QueryParam("sort_column") String sortColumn,
            @QueryParam("sort_direction") String sortDirection,
            @QueryParam("page_number") Integer pageNumber,
            @QueryParam("page_size") Integer pageSize) {
        
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

        return Response.ok(response).build();
    }
} 