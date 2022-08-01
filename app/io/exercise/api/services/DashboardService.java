package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DashboardService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<List<Dashboard>> all (User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .find(Filters.or(
                                Filters.in("readACL", user.getId().toString()),
                                Filters.in("readACL", user.getRoles().get(0).getName()),
                                Filters.eq("readACL", new ArrayList<String>())
                        ))
                        .into(new ArrayList<>());
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not fetch data!")));
            }
        }, ec.current());
    }

    public CompletableFuture<Dashboard> insertOrUpdate(User user, Dashboard dashboard) {
        if(dashboard.getId() == null) {
            return save(dashboard);
        }
        return update(user, dashboard);
    }
    public CompletableFuture<Dashboard> save(Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);

                InsertOneResult result = collection.insertOne(dashboard);
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

    public CompletableFuture<Dashboard> update(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);

                UpdateResult result = collection.replaceOne(Filters.and(
                        Filters.or(
                                Filters.in("writeACL", user.getId().toString()),
                                Filters.in("writeACL", user.getRoles().get(1).getName()),
                                Filters.eq("writeACL", new ArrayList<String>())
                        ),
                        Filters.eq("_id", dashboard.getId())
                ), dashboard);

                if (!result.wasAcknowledged() || result.getMatchedCount() == 0) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not update data!")));
                }

                return dashboard;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }, ec.current());
    }

    public CompletableFuture<Dashboard> delete(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);

                DeleteResult result = collection.deleteOne(Filters.and(
                        Filters.or(
                                Filters.in("writeACL", user.getId().toString()),
                                Filters.in("writeACL", user.getRoles().get(1).getName()),
                                Filters.eq("writeACL", new ArrayList<String>())
                        ),
                        Filters.eq("_id", dashboard.getId())));

                if (!result.wasAcknowledged() || result.getDeletedCount() == 0) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not delete data!")));
                }

                return dashboard;
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
