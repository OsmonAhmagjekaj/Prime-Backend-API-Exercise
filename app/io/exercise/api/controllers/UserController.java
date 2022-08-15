package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.actions.Authenticated;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.User;
import io.exercise.api.services.SerializationService;
import io.exercise.api.services.UserService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;

//@Authenticated
public class UserController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    UserService service;

    public CompletableFuture<Result> all(Http.Request request) {
        return service.all()
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.save(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.update(data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.delete(data, id))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
