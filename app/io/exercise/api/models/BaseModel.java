package io.exercise.api.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.exercise.api.mongo.serializers.ObjectIdDeSerializer;
import io.exercise.api.mongo.serializers.ObjectIdStringSerializer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(of={"id"})
@JsonInclude(Include.NON_NULL)
public @Data class BaseModel implements Cloneable, Serializable {
	@BsonId
	@JsonSerialize(using = ObjectIdStringSerializer.class)
	@JsonDeserialize(using = ObjectIdDeSerializer.class)
	public ObjectId id;

	protected List<String> readACL = new ArrayList<>();

	protected List<String> writeACL = new ArrayList<>();

	@Setter(AccessLevel.NONE)
	@BsonIgnore
	protected Long createdAt;

	protected Long updatedAt;

	public void setId(ObjectId id) {
		if (id == null) {
			this.id = null;
			this.createdAt = null;
			return;
		}
		this.id = id;
		this.createdAt = id.getTimestamp() * 1000L;
	}

	@BsonIgnore
	public Long getLastUpdate() {
		if (updatedAt != null) {
			return updatedAt;
		}
		return createdAt;
	}

	@Override
	public BaseModel clone() throws CloneNotSupportedException {
		BaseModel clone = (BaseModel) super.clone();
		clone.setId(this.getId());
		clone.setUpdatedAt(this.getUpdatedAt());
		return clone;
	}
}
