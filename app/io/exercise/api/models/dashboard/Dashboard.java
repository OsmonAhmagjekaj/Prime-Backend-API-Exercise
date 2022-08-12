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
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

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

    @BsonIgnore
    @BsonProperty("children")
    List<Dashboard> children = new ArrayList<>();

    @BsonIgnore
    @BsonProperty("items")
    List<Content> items = new ArrayList<>();

    @Override
    public Dashboard clone() throws CloneNotSupportedException {
        Dashboard clone = (Dashboard) super.clone();
        clone.setId(this.getId());
        clone.setName(this.getName());
        clone.setDescription(this.getDescription());
        clone.setParentId(this.parentId);
        clone.setChildren(this.getChildren());
        clone.setItems(this.getItems());
        clone.setUpdatedAt(this.getUpdatedAt());
        return clone;
    }
}
