package io.exercise.api.utils;

import io.exercise.api.actions.Attributes;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.Role;
import io.exercise.api.models.User;
import play.mvc.Http;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceUtils {
    public static String getTokenFrom (Http.Request request) {
        Optional<String> optionalToken = request.getHeaders().get("token");
        return optionalToken.orElse(null);
    }

    public static User getUserFrom (Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }

    public static List<String> getRolesFrom (User user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }

    public static boolean containElementsInCommon (List<String> input1, List<String> input2) {
        return input1
                .stream()
                .anyMatch(new HashSet<>(input2)::contains);
    }

    public static boolean hasReadWriteAccess (User user, BaseModel object) {
        return object.getWriteACL().size() == 0
                || object.getWriteACL().contains(user.getId().toString())
                || containElementsInCommon(object.getWriteACL(), getRolesFrom(user));
    }
}
