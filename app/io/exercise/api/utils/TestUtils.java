package io.exercise.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Dashboard;
import org.bson.types.ObjectId;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TestUtils {
    public static Http.RequestBuilder requestBuilder (String method, String uri, String headerKey, String headerValue, JsonNode body) {
        return new Http.RequestBuilder()
                .method(method.toUpperCase())
                .uri(uri)
                .header(headerKey, headerValue)
                .bodyJson(body);
    }

    public static Http.RequestBuilder requestBuilder (String method, String uri, JsonNode body) {
        return new Http.RequestBuilder()
                .method(method.toUpperCase())
                .uri(uri)
                .bodyJson(body);
    }

    public static Dashboard dashboardBuilder (String name, ObjectId id, ObjectId parentId, User user) {
        Dashboard dashboard = new Dashboard(name, name + " description", parentId, new ArrayList<>(), new ArrayList<>());
        dashboard.setReadACL(List.of(user.getId().toString()));
        dashboard.setWriteACL(List.of(user.getId().toString()));
        dashboard.setId(Objects.requireNonNullElseGet(id, ObjectId::new));
        return dashboard;
    }
}
