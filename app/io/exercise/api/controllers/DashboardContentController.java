package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.actions.Authenticated;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.dashboard.Content;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.services.DashboardContentService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;

@Authenticated
public class DashboardContentController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardContentService service;

    public CompletableFuture<Result> all(Http.Request request, String id) {
        return service.all(ServiceUtils.getUserFrom(request), id)
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation(type = Content.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.save(ServiceUtils.getUserFrom(request), data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation(type = Content.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, Content.class)
                .thenCompose((data) -> service.update(ServiceUtils.getUserFrom(request), data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

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
