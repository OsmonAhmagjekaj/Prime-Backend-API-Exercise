package io.exercise.api.controllers;

import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.requests.AuthUserRequest;
import io.exercise.api.services.AuthenticateService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 *  AuthenticateController contains methods used to generate a token for a user.
 * Created by Osmon on 15/08/2022
 */
public class AuthenticateController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    AuthenticateService service;

    /**
     * Create a token for a user
     * @param request request containing the user to be authenticated with a token
     * @return result containing the token as a String
     * @throws JWTCreationException in case of invalid singing configuration
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.AuthenticateService
     */
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
