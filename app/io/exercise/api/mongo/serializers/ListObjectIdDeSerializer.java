package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListObjectIdDeSerializer extends JsonDeserializer<List<ObjectId>> {

	@Override
	public List<ObjectId> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		List<ObjectId> result = new ArrayList<>();
		if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;

			for (int i =0; i< arrayNode.size(); i++) {
				ObjectId next = this.objectIdFromNode(arrayNode.get(i));
				if (next != null) {
					result.add(next);
				}
			}
		} else {
			ObjectId next = this.objectIdFromNode(node);
			if (next != null) {
				result.add(next);
			}
		}
		return result;
	}

	public ObjectId objectIdFromNode (JsonNode which) {
		if(which.get("$oid") != null) {
			String objectId = which.get("$oid").asText();
			return new ObjectId(objectId);
		}
		if (which.isTextual()) {
			return new ObjectId(which.asText());
		}
		return null;
	}
}
