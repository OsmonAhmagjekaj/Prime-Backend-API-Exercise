package io.exercise.api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.requests.AuthUserRequest;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 *  AuthenticateService contains service methods for AuthenticateController.
 * Created by Osmon on 15/08/2022
 */
public class AuthenticateService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    public static final long ONE_DAY_IN_MILLIS = 1000L * 60 * 60 * 24;

    /**
     * Create a token for a user
     * @param userRequest the user to be authenticated with a token
     * @return the token as a String
     * @throws JWTCreationException in case of invalid singing configuration
     * @throws CompletionException in case data is not found or an internal error occurred
     * @throws MongoException in case mongo operations fail
     * @see io.exercise.api.controllers.AuthenticateController
     */
    public CompletableFuture<String> authenticate (AuthUserRequest userRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                User user = collection.find(Filters.or(
                        Filters.eq("username", userRequest.getUsername()),
                        Filters.eq("email", userRequest.getUsername()))
                ).first();

                if (user == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User doesn't exist!")));
                }

                if (!Hash.checkPassword(userRequest.getPassword(), user.getPassword())) {
                    throw new CompletionException(new RequestException(Http.Status.UNAUTHORIZED, Json.toJson("Bad Credentials!")));
                }

                String secret = config.getString("play.http.secret.key");
                Algorithm algorithm = Algorithm.HMAC256(secret);
                return JWT.create()
                        .withIssuer(user.getId().toString())
                        .withExpiresAt(new Date(System.currentTimeMillis() + ONE_DAY_IN_MILLIS))
                        .sign(algorithm);
            } catch (JWTCreationException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid Singing configuration / Couldn't convert Claims.")));
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }
}
