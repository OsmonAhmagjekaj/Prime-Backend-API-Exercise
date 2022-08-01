package io.exercise.api.utils;

import com.google.inject.Inject;
import io.exercise.api.actions.Attributes;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import play.mvc.Http;

import com.typesafe.config.Config;
import java.util.Optional;

public class ServiceUtils {
    public static String getTokenFrom (Http.Request request) {
        Optional<String> optionalToken = request.getHeaders().get("token");
        return optionalToken.orElse(null);
    }

    public static User getUserFrom (Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }
}
