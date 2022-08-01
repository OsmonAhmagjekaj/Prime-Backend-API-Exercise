package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.actions.Authenticated;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.services.DashboardService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;

@Authenticated
public class DashboardController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    DashboardService service;

    public CompletableFuture<Result> all(Http.Request request) {
        return service.all(ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation(type = Dashboard.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> insertOrUpdate(Http.Request request) {
        return serializationService.parseBodyOfType(request, Dashboard.class)
                .thenCompose((dashboard) -> service.insertOrUpdate(ServiceUtils.getUserFrom(request), dashboard))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

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
