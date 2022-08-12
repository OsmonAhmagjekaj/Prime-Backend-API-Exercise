package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.exercise.api.utils.ServiceUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonIgnore;

import javax.validation.constraints.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {

    @NotEmpty(message = "cannot be empty")
    @Size(min = 5, message = "should have at least 5 characters")
    String username;

    @NotEmpty(message = "cannot be empty")
    @Size(min = 5, message = "should have at least 5 characters")
    String password;

    List<String> roles = new ArrayList<>();

    @JsonIgnore
    @BsonIgnore
    public boolean hasReadWriteAccessFor (BaseModel object) {
        return (object.getWriteACL().size() == 0 && object.getReadACL().size() == 0)
                || object.getWriteACL().contains(this.getId().toString())
                || object.getWriteACL().contains("*")
                || ServiceUtils.containElementsInCommon(object.getWriteACL(), this.getRoles());
    }

    @JsonIgnore
    @BsonIgnore
    public List<String> getAccessIds () {
        return Stream.of(List.of(this.getId().toString()), this.getRoles(), List.of("*"))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
