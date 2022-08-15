package io.exercise.api.actions;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import play.libs.Json;
import play.mvc.*;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

/**
 *  An action class used to authenticate a user
 */
public class AuthenticatedAction extends Action<Authenticated> {
    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            String token = ServiceUtils
                    .getTokenFrom(request)
                    .join();

            User user = ServiceUtils
                    .extractIdFrom(token)
                    .thenCompose((id) -> ServiceUtils.getUserFrom(mongoDB, id))
                    .thenCompose((usr) -> ServiceUtils.verifyTokenFor(usr, token, config))
                    .join();

            request = request.addAttr(Attributes.USER_TYPED_KEY, user);
            return delegate.call(request);
        } catch (JWTVerificationException ex) {
            ex.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid signature/claims.")));
        } catch (CompletionException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Invalid")));
        }
    }
}
