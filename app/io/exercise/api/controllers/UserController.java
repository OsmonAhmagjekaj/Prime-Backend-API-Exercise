package io.exercise.api.controllers;

import com.google.inject.Inject;
import com.mongodb.MongoException;
import io.exercise.api.actions.Authenticated;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.User;
import io.exercise.api.services.SerializationService;
import io.exercise.api.services.UserService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * UserController handles CRUD operations for users.
 * Created by Osmon on 15/08/2022
 */
public class UserController extends Controller {

    @Inject
    SerializationService serializationService;

    @Inject
    UserService service;

    /**
     * Get a list of all the users
     * @param skip number of users to skip per page
     * @param limit number of users to limit per page
     * @param request request that contains the user token
     * @return result containing all users
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.UserService
     */
    @Authenticated
    public CompletableFuture<Result> all(int skip, int limit, Http.Request request) {
        return service.all(skip, limit, ServiceUtils.getUserFrom(request))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Save a user into the database
     * @param request request that contains the user to be added and the authenticated user
     * @return result containing the added user
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.UserService
     */
    @Validation(type = User.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.save(data))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Update a user in the database
     * @param request request that contains the user to be updated and the authenticated user
     * @return result containing the updated user
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.UserService
     */
    @Authenticated
    @Validation(type = User.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.update(data, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    /**
     * Delete a user from the database
     * @param request request that contains the user to be deleted and and the authenticated user
     * @return result containing the deleted user
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.UserService
     */
    @Authenticated
    @Validation(type = User.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose((data) -> service.delete(data, id, ServiceUtils.getUserFrom(request)))
                .thenCompose((data) -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
