package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.requests.AuthUserRequest;
import io.exercise.api.services.AuthenticateService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;

public class AuthenticateController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    AuthenticateService service;

    @Validation(type = AuthUserRequest.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> authenticate(Http.Request request) {
        return serializationService.parseBodyOfType(request, AuthUserRequest.class)
                .thenCompose((data) -> service.authenticate(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
