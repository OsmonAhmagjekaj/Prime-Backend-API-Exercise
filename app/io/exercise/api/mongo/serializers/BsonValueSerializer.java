package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import play.Logger;

import java.io.IOException;
import java.util.Set;

public class BsonValueSerializer extends JsonSerializer<BsonValue> {

    @Override
    public void serialize(BsonValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
    	if(value == null) {
        	jgen.writeNull();
        } else {
        	switch (value.getBsonType()) {
        	case DOUBLE:
            	jgen.writeNumber(value.asDouble().getValue());
            	break;
        	case INT32:
            	jgen.writeNumber(value.asInt32().getValue());
            	break;
        	case INT64:
            	jgen.writeNumber(value.asInt64().getValue());
            	break;
        	case STRING:
            	jgen.writeString(value.asString().getValue());
            	break;
        	case DECIMAL128:
        		// TODO: test this
            	jgen.writeNumber(Double.parseDouble(value.asDecimal128().getValue().toString()));
            	break;
        	case DOCUMENT:
        		jgen.writeStartObject();
        		BsonDocument document = value.asDocument();
        		Set<String> keys = document.keySet();
        		for (String key: keys) {
        			this.serialize(document.get(key), jgen, provider);
        		}
        		jgen.writeEndObject();
        		break;
        	case ARRAY:
        		jgen.writeStartArray();
        		BsonArray array = value.asArray();
        		for (BsonValue next: array) {
        			this.serialize(next, jgen, provider);
        		}
        		jgen.writeEndArray();
			case BINARY:
				jgen.writeBinary(value.asBinary().getData());
				break;
			case BOOLEAN:
				jgen.writeBoolean(value.asBoolean().getValue());
				break;
			case DATE_TIME:
				jgen.writeNumber(value.asDateTime().getValue());
				break;
			case DB_POINTER:
	            jgen.writeStartObject();
	            jgen.writeStringField("$oid", value.asDBPointer().getId().toString());
	            jgen.writeEndObject();
				break;
			case END_OF_DOCUMENT:
				Logger.of(this.getClass()).debug("End of Document");
				break;
			case JAVASCRIPT:
				jgen.writeString(value.asJavaScript().getCode());
				break;
			case JAVASCRIPT_WITH_SCOPE:
				break;
			case MAX_KEY:
				break;
			case MIN_KEY:
//				throw new JsonProcessingException("Unhandled MIN KEY");
				break;
			case NULL:
				jgen.writeNull();
				break;
			case OBJECT_ID:
	            jgen.writeStartObject();
	            jgen.writeStringField("$oid", value.asObjectId().getValue().toString());
	            jgen.writeEndObject();
				break;
			case REGULAR_EXPRESSION:
//				value.asRegularExpression().get
				break;
			case SYMBOL:
				jgen.writeString(value.asSymbol().getSymbol());
				break;
			case TIMESTAMP:
				jgen.writeNumber(value.asTimestamp().getValue());
				break;
			case UNDEFINED:
				break;
			default:
				break;
        	}
        }
    }
    
//    private void serializeBson(value, jpgen, provider) {
//    	
//    }
}
