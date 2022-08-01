package io.exercise.api.models.dashboard;

import io.exercise.api.models.enums.DashboardContentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@BsonDiscriminator(key = "type", value = "EMAIL")
public class EmailContent extends Content {

    @NotEmpty(message = "cannot be empty")
    protected String text;

    @NotEmpty(message = "cannot be empty")
    protected String subject;

    @NotEmpty(message = "cannot be empty")
    protected String email;

    @Override
    public DashboardContentType getType() {
        return DashboardContentType.EMAIL;
    }
}
