package io.exercise.api.mongo.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.List;

public class ListObjectIdSerializer extends JsonSerializer<List<ObjectId>> {

    @Override
    public void serialize(List<ObjectId> value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        if(value == null) {
        	jgen.writeNull();
        } else {
            jgen.writeStartArray();
            for (ObjectId id: value) {
                jgen.writeString(id.toString());
            }
            jgen.writeEndArray();
        }
    }
}
