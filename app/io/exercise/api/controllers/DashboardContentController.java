package io.exercise.api.controllers;

import com.google.inject.Inject;
import com.mongodb.MongoException;
import io.exercise.api.actions.Authenticated;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.dashboard.Content;
import io.exercise.api.services.DashboardContentService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * DashboardContentController contains methods for CRUD operations on dashboard contents.
 * Created by Osmon on 15/08/2022
 */
@Authenticated
public class DashboardContentController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardContentService service;

    /**
     * Get a list of all the dashboard contents
     * @param skip number of dashboard contents to skip per page
     * @param limit number of dashboard contents to limit per page
     * @param request request that contains the user token
     * @return result containing all dashboard contents
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardContentService
     */
    public CompletableFuture<Result> all(int skip, int limit, Http.Request request, String id) {
        return service.all(limit, skip, ServiceUtils.getUserFrom(request), id)
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Save a dashboard content into the database
     * @param request request that contains the dashboard content and the user token
     * @return result containing the added dashboard content
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardContentService
     */
    @Validation(type = Content.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.save(ServiceUtils.getUserFrom(request), data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Update a dashboard content in the database
     * @param request request that contains the dashboard content and the user token
     * @return result containing the updated dashboard content
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardContentService
     */
    @Validation(type = Content.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.update(ServiceUtils.getUserFrom(request), data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Delete a dashboard content from the database
     * @param request request that contains the dashboard content and the user token
     * @return result containing the deleted dashboard content
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.DashboardContentService
     */
    @Validation(type = Content.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.delete(ServiceUtils.getUserFrom(request), data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
