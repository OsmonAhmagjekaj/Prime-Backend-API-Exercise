package io.exercise.api.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

/**
 * Created by Agon on 09/08/2020.
 */
public interface IMongoDB {
	public MongoDatabase getMongoDatabase();
	public MongoClient getMongoClient();
}
