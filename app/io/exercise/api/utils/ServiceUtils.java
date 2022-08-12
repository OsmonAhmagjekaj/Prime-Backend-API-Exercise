package io.exercise.api.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actions.Attributes;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ServiceUtils {

    public static CompletableFuture<String> getTokenFrom (Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<String> optionalToken = request.getHeaders().get("token");
            return optionalToken.orElse(null);
        });
    }

    public static CompletableFuture<String> extractIdFrom (String token) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] decodedBytes = Base64.getDecoder().decode(token.split("\\.")[1]);
            String decodedToken = new String(decodedBytes);
            return Json.parse(decodedToken).get("iss").asText();
        });
    }

    public static CompletableFuture<User> getUserFrom (IMongoDB mongoDB, String id) {
        return CompletableFuture.supplyAsync(() -> {
            MongoCollection<User> collection = mongoDB.getMongoDatabase()
                    .getCollection("users", User.class);

            User user = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            if (user == null) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User doesn't exist!")));
            }

            return user;
        });
    }

    public static CompletableFuture<User> verifyTokenFor (User user, String token, Config config) {

        return CompletableFuture.supplyAsync(() -> {
            String secret = config.getString("play.http.secret.key");
            Algorithm algorithm;
            try {
                algorithm = Algorithm.HMAC256(secret);
            } catch (UnsupportedEncodingException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "Bad Token: " + e.getMessage()));
            }

            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(user.getId().toString())
                    .build();
            verifier.verify(token);

            return user;
        });
    }

    public static User getUserFrom (Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }

    public static boolean containElementsInCommon (List<String> input1, List<String> input2) {
        return input1.stream().anyMatch(new HashSet<>(input2)::contains);
    }

    public static Bson getReadAccessFilterFor (List<String> accessIds) {
        return Filters.or(
                Filters.in("readACL", accessIds),
                Filters.in("writeACL", accessIds),
                Filters.and(
                        Filters.eq("readACL", new ArrayList<String>()),
                        Filters.eq("writeACL", new ArrayList<String>())
                )
        );
    }
}
