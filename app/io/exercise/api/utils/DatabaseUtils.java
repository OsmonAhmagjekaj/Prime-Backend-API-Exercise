package io.exercise.api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.exercise.api.exceptions.RequestException;
import org.bson.Document;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionException;

import static play.mvc.Results.status;

public class DatabaseUtils {

	public static Result throwableToResult (Throwable error) {
		Result status = DatabaseUtils.statusFromThrowable(error);
		if (status != null) {
			return status;
		}
		ObjectNode result = Json.newObject();
		result.put("status", 501);
		result.put("message", error.getLocalizedMessage());
		return status(501, result);
	}

	public static Result statusFromThrowable (Throwable error) {
		if (error instanceof RequestException) {
			return statusFromThrowable((RequestException) error);
		}
		if (error.getCause() == null) {
			return null;
		}
		return statusFromThrowable(error.getCause());
	}

	public static Result statusFromThrowable (RequestException error) {
		return status(error.getStatusCode(), objectNodeFromError(error.getStatusCode(), error.getMessage()));
	}

	public static ObjectNode objectNodeFromError (int status, String message) {
		ObjectNode result = Json.newObject();
		result.put("status", status);
		result.put("message", message);
		return result;
	}


	public static <T> List<T> parseJsonListOfType (JsonNode json, Class<T> type) {
		try {
			if (!json.isArray()) {
				throw new RequestException(Http.Status.BAD_REQUEST, "invalid_parameters");
			}
			List<T> list = new ArrayList<>();
			for (JsonNode node: json) {
				list.add(Json.fromJson(node, type));
			}
			return list;
		} catch (RequestException | ClassCastException ex) {
			ex.printStackTrace();
			throw new CompletionException(ex);
		} catch (CompletionException ex) {
			ex.printStackTrace();
			throw ex;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "service_unavailable"));
		}
	}


	/**
	 * Converts a Document to a JSON node
	 * @param item
	 * @return
	 */
	public static JsonNode toJson (Document item) {
		return Json.parse(item.toJson());
	}


	/**
	 * Converts a Document to a JSON node
	 * @param items
	 * @return
	 */
	public static JsonNode toJson (List<Document> items) {
		ArrayNode array = Json.newArray();
		for (Document item: items) {
			array.add(DatabaseUtils.toJson(item));
		}
		return array;
	}

	/**
	 * parses a JSON object node and converts it to a mongodb java driver
	 * Document
	 * @param value
	 * @return
	 */
	public static Document toDocument (ObjectNode value) {
		return Document.parse(value.toString());
	}


	/**
	 * Parses an array node to a list of documents
	 * @param json
	 * @return
	 */
	public static List<Document> toListDocument(ArrayNode json) {
		List<Document> result = new ArrayList<>();
		for (JsonNode node : json) {
			result.add(toDocument((ObjectNode) node));
		}
		return result;
	}
}
