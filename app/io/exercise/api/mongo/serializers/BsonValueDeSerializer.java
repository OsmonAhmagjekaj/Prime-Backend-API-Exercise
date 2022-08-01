package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.*;
import play.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

public class BsonValueDeSerializer extends JsonDeserializer<BsonValue> {

	@Override
	public BsonValue deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		JsonNode which = jp.getCodec().readTree(jp);
		return this.toBsonValue(which);
	}

	private BsonValue toBsonValue(JsonNode which) throws IOException, JsonProcessingException {
		// TODO Auto-generated method stub
		if (which == null) {
			return new BsonNull();
		}
		switch (which.getNodeType()) {
			case STRING:
				return new BsonString(which.asText());
			case NUMBER:
				if (which.isLong()) {
					return new BsonInt64(which.asLong());
				} else if (which.isInt()) {
					return new BsonInt32(which.asInt());
				} else if (which.isFloat()) {
					return new BsonDouble(which.floatValue());
				} else if (which.isDouble()) {
					return new BsonDouble(which.asDouble());
				} else if (which.isShort() && which.canConvertToInt()) {
					return new BsonInt32(which.intValue());
				}
				break;
			case OBJECT: {
				// TODO: Check Object ID can be one of the objects
				ObjectNode object = (ObjectNode) which;
				BsonDocument result = new BsonDocument();
				Iterator<Entry<String, JsonNode>> iterator = object.fields();
				while (iterator.hasNext()) {
					Entry<String, JsonNode> next = iterator.next();
					result.put(next.getKey(), this.toBsonValue(next.getValue()));
				}
				return result;
			}
			case ARRAY: {
				ArrayNode array = (ArrayNode) which;
				BsonArray result = new BsonArray();
				for (JsonNode node: array) {
					result.add(this.toBsonValue(node));
				}
				return result;
			}
			case BINARY:
				return new BsonBinary(which.binaryValue());
			case BOOLEAN:
				return new BsonBoolean(which.asBoolean());
			case MISSING:
			case NULL:
				return new BsonNull();
		}
		
//		throw new Exception("cannot handle this");
		Logger.of(this.getClass()).debug("Cannot convert back node of type: " + which.getNodeType());
		Logger.of(this.getClass()).debug("With Value: " + which.toString());
		//which.toString() + 
		return null;
	}
}
