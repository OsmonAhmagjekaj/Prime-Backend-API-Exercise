package io.exercise.api.actions;

import io.exercise.api.models.User;
import play.libs.typedmap.TypedKey;

public class Attributes {
    public static final TypedKey<User> USER_TYPED_KEY = TypedKey.<User>create("user");
}
