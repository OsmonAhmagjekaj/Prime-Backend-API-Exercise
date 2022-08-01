package io.exercise.api.models.dashboard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.exercise.api.models.BaseModel;
import io.exercise.api.mongo.serializers.ObjectIdDeSerializer;
import io.exercise.api.mongo.serializers.ObjectIdStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class Dashboard extends BaseModel {
    @NotEmpty(message = "cannot be empty")
    @Size(min = 2, message = "should have at least 3 characters")
    private String name;

    @NotEmpty(message = "cannot be empty")
    @Size(min = 4, message = "should have at least 4 characters")
    private String description;

    @JsonSerialize(using = ObjectIdStringSerializer.class)
    @JsonDeserialize(using = ObjectIdDeSerializer.class)
    private ObjectId parentId;
}
