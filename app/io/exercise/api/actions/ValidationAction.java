package io.exercise.api.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import io.exercise.api.models.validators.HibernateValidator;
import play.libs.Json;
import play.mvc.Action;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 *  An action class used to validate objects of all types, using hibernate and casting validation.
 *  Create by Osmon on 08/15/2022
 */
public class ValidationAction extends Action<Validation> {

    @Override
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> call(Http.Request request) {
        try {
            JsonNode body = request.body().asJson();
            Object object = Json.fromJson(body, configuration.type());

            String errors = HibernateValidator.validate(object);
            if (!Strings.isNullOrEmpty(errors)) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(errors)));
            }

            return delegate.call(request);
        } catch (Exception ex) {
            ex.printStackTrace();
            ObjectNode response = Json.newObject();
            response.put("message", "Invalid object supplied, cannot cast to the specific type.");
            return CompletableFuture.completedFuture(badRequest(response));
        }
    }
}
