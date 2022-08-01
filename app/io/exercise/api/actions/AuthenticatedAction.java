package io.exercise.api.actions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.*;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class AuthenticatedAction extends Action<Authenticated> {
    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            String token = ServiceUtils.getTokenFrom(request);
            byte[] decodedBytes = Base64.getDecoder().decode(token.split("\\.")[1]);
            String decodedToken = new String(decodedBytes);
            String id = Json.parse(decodedToken).get("iss").asText();

            MongoCollection<User> collection = mongoDB.getMongoDatabase()
                    .getCollection("users", User.class);

            User user = collection.find(Filters.eq("_id", new ObjectId(id))).first();
            if (user == null) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User doesn't exist!")));
            }

            String secret = config.getString("play.http.secret.key");
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer(user.getId().toString())
                    .build();
            verifier.verify(token);

            request = request.addAttr(Attributes.USER_TYPED_KEY, user);
            return delegate.call(request);
        } catch (JWTVerificationException ex) {
            ex.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid signature/claims.")));
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid")));
        }
    }
}
