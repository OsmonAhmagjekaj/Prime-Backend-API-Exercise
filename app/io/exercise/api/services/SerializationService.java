package io.exercise.api.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.utils.DatabaseUtils;
import org.bson.Document;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Http.Request;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Singleton
public class SerializationService {
    @Inject
    HttpExecutionContext ec;

    @Inject
    ObjectMapper mapper;

    public <T> CompletableFuture<JsonNode> toJsonNode(T result) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return Json.toJson(result);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            }
        }, ec.current());
    }

    public CompletableFuture<Document> parseBody(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode json = request.body().asJson();
                if (!json.isObject()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters");
                }
                return DatabaseUtils.toDocument((ObjectNode) json);
            } catch (RequestException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            }
        }, ec.current());
    }

    public <T> CompletableFuture<T> parseBodyOfType(Request request, Class<T> valueType) {
        return CompletableFuture.supplyAsync(() -> this.syncParseBodyOfType(request, valueType), ec.current());
    }

    public <T> T syncParseBodyOfType (Request request, Class<T> valueType) {
        try {
            Optional<T> body = request.body().parseJson(valueType);
            if (!body.isPresent()) {
                throw new RequestException(Http.Status.BAD_REQUEST, "parsing_exception");
            }
            return body.get();
        } catch (RequestException ex) {
            ex.printStackTrace();
            throw new CompletionException(ex);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "service_unavailable"));
        }
    }

    public CompletableFuture<List<Document>> parseListBody(Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode json = request.body().asJson();
                if (!json.isArray()) {
                    throw new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters");
                }
                return DatabaseUtils.toListDocument((ArrayNode) json);
            } catch (RequestException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            }
        }, ec.current());
    }

    public <T> CompletableFuture<List<T>> parseFileOfType(Request request, String key, Class<T> valueType) {
        return CompletableFuture.supplyAsync(() -> {
            Http.MultipartFormData<File> data = request.body().asMultipartFormData();
            if (data.getFiles().size() == 0) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters"));
            }
            File file = data.getFile(key).getRef();
            try {
                JsonNode content = this.fileToObjectNode(file);
                return DatabaseUtils.parseJsonListOfType(content, valueType);
            } catch (JsonProcessingException e) {
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "parsing_exception"));
            } catch (Exception e) {
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "service_unavailable"));
            }
        }, ec.current());
    }


    public JsonNode fileToObjectNode (File which) throws IOException {
        try (JsonParser parser = mapper.getFactory().createParser(which)) {
            return mapper.readTree(parser);
        }
    }

    public <T> CompletableFuture<List<T>> parseListBodyOfType (Request request, Class<T> type) {
        return CompletableFuture.supplyAsync(() -> this.syncParseListBodyOfType(request, type), ec.current());
    }

    public <T> List<T> syncParseListBodyOfType (Request request, Class<T> type) {
        JsonNode json = request.body().asJson();
        return DatabaseUtils.parseJsonListOfType(json, type);
    }

}
