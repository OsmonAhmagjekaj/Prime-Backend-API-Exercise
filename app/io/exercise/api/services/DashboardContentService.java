package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Content;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DashboardContentService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<List<Content>> all (User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class)
                        .find(Filters.and(
                                Filters.eq("dashboardId", new ObjectId(id)),
                                Filters.or(
                                        Filters.in("readACL", user.getId().toString()),
                                        Filters.in("readACL", user.getRoles().get(0).getName()),
                                        Filters.eq("readACL", new ArrayList<String>())
                                )
                        ))
                        .into(new ArrayList<>());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not fetch data!")));
            }
        }, ec.current());
    }

    public CompletableFuture<Content> save(User user, Content content, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Content> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);
                content.setDashboardId(new ObjectId(id));

                InsertOneResult result = collection.insertOne(content);
                if (!result.wasAcknowledged() || result.getInsertedId() == null) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not insert data!")));
                }

                return collection.find(Filters.eq("_id", result.getInsertedId())).first();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }, ec.current());
    }

    public CompletableFuture<Content> update(User user, Content content, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Content> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);

                content.setDashboardId(new ObjectId(id));
                UpdateResult result = collection.replaceOne(Filters.and(
                        Filters.or(
                                Filters.in("writeACL", user.getId().toString()),
                                Filters.in("writeACL", user.getRoles().get(1).getName()),
                                Filters.eq("writeACL", new ArrayList<String>())
                        ),
                        Filters.eq("_id", content.getId())
                ), content);

                if (!result.wasAcknowledged() || result.getMatchedCount() == 0) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not update data!")));
                }

                return content;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }, ec.current());
    }

    public CompletableFuture<Content> delete(User user, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Content> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);

                DeleteResult result = collection.deleteOne(Filters.and(
                        Filters.or(
                                Filters.in("writeACL", user.getId().toString()),
                                Filters.in("writeACL", user.getRoles().get(1).getName()),
                                Filters.eq("writeACL", new ArrayList<String>())
                        ),
                        Filters.eq("_id", content.getId())));

                if (!result.wasAcknowledged() || result.getDeletedCount() == 0) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not delete data!")));
                }

                return content;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Internal error!")));
            }
        }, ec.current());
    }
}
