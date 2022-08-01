package io.exercise.api.models.dashboard;

import io.exercise.api.models.CategoryValueData;
import io.exercise.api.models.enums.DashboardContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "LINE")
public class LineContent extends Content {

    @NotEmpty(message = "cannot be empty")
    List<CategoryValueData> data;

    @Override
    public DashboardContentType getType() {
        return DashboardContentType.LINE;
    }
}
