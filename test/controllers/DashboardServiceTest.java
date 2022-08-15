package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.TestUtils;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.route;

public class DashboardServiceTest extends WithApplication {
    public User user;
    public String authenticatedUserToken;

    @Before
    public void setup() {
        // Create a user
        user = new User("oprime", "password", new ArrayList<>());
        user.setId(new ObjectId("61aa320afc13ae31a1000141"));
        final Http.RequestBuilder createUserRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/user/")
                .bodyJson(Json.toJson(user));
        route(app, createUserRequest);

        // Authenticate the created user
        final Http.RequestBuilder authenticateRequest = new Http.RequestBuilder()
                .method("POST")
                .uri("/api/authenticate/")
                .bodyJson(Json.toJson(user));
        final Result result = route(app, authenticateRequest);
        JsonNode body = Json.parse(contentAsString(result));
        authenticatedUserToken = Json.fromJson(body, String.class);

        // Generate some dummy dashboard data to test with
        String id = "62ea320afc13ae31a100013";
        AtomicInteger integer = new AtomicInteger(0);
        Stream.generate(() -> TestUtils.requestBuilder(
                "POST",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(TestUtils.dashboardBuilder("Dashboard " + integer.get(), new ObjectId(id + integer.getAndIncrement()), null, user)))
        ).limit(5)
        .forEach(request -> route(app, request));
    }

    @Test
    public void testAll() {
        final Http.RequestBuilder allRequest = TestUtils.requestBuilder(
                "GET",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(""));
        final Result result = route(app, allRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        List<Dashboard> returnedResult = DatabaseUtils.parseJsonListOfType(body, Dashboard.class);
        assertTrue("Expected the collection to have elements!", returnedResult.size() != 0);
    }

    @Test
    public void testSave() {
        Dashboard dashboard = TestUtils.dashboardBuilder("Dashboard", new ObjectId("62ea320afc13ae31a1000136"), new ObjectId("62ea320afc13ae31a1000139"), user);
        final Http.RequestBuilder saveRequest = TestUtils.requestBuilder(
                "POST",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard));
        final Result result = route(app, saveRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        Dashboard returnedResult = Json.fromJson(body, Dashboard.class);
        assertEquals("Expected the input user", returnedResult, dashboard);
    }

    @Test
    public void testBadSave() {
        Dashboard dashboard = TestUtils.dashboardBuilder("D", null, new ObjectId("62ea320afc13ae31a1000139"), user);
        final Http.RequestBuilder saveRequest = TestUtils.requestBuilder(
                "POST",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard)
        );
        final Result result = route(app, saveRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.BAD_REQUEST, result.status());
    }

    @Test
    public void testUpdate() {
        Dashboard dashboard = TestUtils.dashboardBuilder("Dashboard1 Updated", new ObjectId("62ea320afc13ae31a1000130"), null, user);
        final Http.RequestBuilder updateRequest = TestUtils.requestBuilder(
                "PUT",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard)
        );
        final Result result = route(app, updateRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        Dashboard returnedResult = Json.fromJson(body, Dashboard.class);
        assertEquals("Expected the input user", returnedResult, dashboard);
    }

    @Test
    public void testUpdateBadId() {
        Dashboard dashboard = TestUtils.dashboardBuilder("Dashboard1 Updated", new ObjectId("61ea322afc13be31a1000130"), null, user);
        final Http.RequestBuilder updateRequest = TestUtils.requestBuilder(
                "PUT",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard)
        );
        final Result result = route(app, updateRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.NOT_FOUND, result.status());
    }

    @Test
    public void testDelete() {
        Dashboard dashboard = TestUtils.dashboardBuilder("Dashboard", new ObjectId("62ea120afc13ae31a1000139"), new ObjectId("62ea320afc13ae31a1000137"), user);
        final Http.RequestBuilder saveRequest = TestUtils.requestBuilder(
                "POST",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard)
        );
        route(app, saveRequest);

        final Http.RequestBuilder deleteRequest = TestUtils.requestBuilder(
                "DELETE",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard)
        );
        final Result result = route(app, deleteRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.OK, result.status());

        JsonNode body = Json.parse(contentAsString(result));
        Dashboard returnedResult = Json.fromJson(body, Dashboard.class);
        assertEquals("Expected the input user", returnedResult, dashboard);
    }

    @Test
    public void testDeleteBadId() {
        Dashboard dashboard = TestUtils.dashboardBuilder("Dashboard", new ObjectId("62ea321afc13ae31a1000140"), new ObjectId("62ea320afc13ae31a1000139"), user);
        final Http.RequestBuilder deleteRequest = TestUtils.requestBuilder(
                "DELETE",
                "/api/dashboard/",
                "token",
                authenticatedUserToken,
                Json.toJson(dashboard)
        );
        final Result result = route(app, deleteRequest);

        assertEquals("application/json", result.contentType().get());
        assertEquals(Http.Status.NOT_FOUND, result.status());
    }

    @After
    public void cleanUp() {
    }
}
