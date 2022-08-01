package io.exercise.api.models.dashboard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.enums.DashboardContentType;
import io.exercise.api.mongo.serializers.ObjectIdDeSerializer;
import io.exercise.api.mongo.serializers.ObjectIdStringSerializer;
import lombok.*;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, property="type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value= EmailContent.class, name = "EMAIL"),
        @JsonSubTypes.Type(value= ImageContent.class, name = "IMAGE"),
        @JsonSubTypes.Type(value= LineContent.class, name = "LINE"),
        @JsonSubTypes.Type(value= TextContent.class, name = "TEXT")
})
@BsonDiscriminator(key = "type", value = "NONE")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Content extends BaseModel {

    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    protected ObjectId dashboardId;

    @BsonIgnore
    protected DashboardContentType type = DashboardContentType.NONE;
}
