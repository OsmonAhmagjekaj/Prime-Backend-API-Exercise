package io.exercise.api.controllers;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import io.exercise.api.actions.Authenticated;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.services.DashboardService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * DashboardController contains methods for CRUD operations on dashboards.
 * Created by Osmon on 15/08/2022
 */
@Authenticated
public class DashboardController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardService service;

    /**
     * Get a list of all the dashboards together with their items
     * @param skip number of dashboards to skip per page
     * @param limit number of dashboards to limit per page
     * @param request request that contains the user token
     * @return result containing all dashboards
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardService
     */
    public CompletableFuture<Result> all(int skip, int limit, Http.Request request) {
        return service.all(skip, limit, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Get a list of all the dashboards in a hierarchial manner, together with their items
     * @param skip number of dashboards to skip per page
     * @param limit number of dashboards to limit per page
     * @param request request that contains the user token
     * @return result containing all dashboards in a hierarchical manner
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardService
     */
    public CompletableFuture<Result> hierarchy(int skip, int limit, Http.Request request) {
        return service.hierarchy(skip, limit, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Save a dashboard into the database
     * @param request request that contains the dashboard and the user token
     * @return result containing the added dashboard
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardService
     */
    @Validation(type = Dashboard.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboard) -> service.save(ServiceUtils.getUserFrom(request), dashboard))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Update a dashboard in the database
     * @param request request that contains the dashboard and the user token
     * @return result containing the updated dashboard
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardService
     */
    @Validation(type = Dashboard.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboard) -> service.update(ServiceUtils.getUserFrom(request), dashboard))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Delete a dashboard from the database
     * @param request request that contains the dashboard and the user token
     * @return result containing the deleted dashboard
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardService
     */
    @Validation(type = Dashboard.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> delete(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboard) -> service.delete(ServiceUtils.getUserFrom(request), dashboard))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
