package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdDeSerializer extends JsonDeserializer<ObjectId> {

	@Override
	public ObjectId deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		if(node.get("$oid") != null) {
			String objectId = node.get("$oid").asText();
			return new ObjectId(objectId);
		}
		if (node.isTextual()) {
			return new ObjectId(node.asText());
		}
		return null;
	}
}
