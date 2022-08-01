package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.InsertOneResult;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
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
                                Filters.in("readACL", ServiceUtils.getRolesFrom(user)),
                                Filters.in("writeACL", user.getId().toString()),
                                Filters.in("writeACL", ServiceUtils.getRolesFrom(user)),
                                Filters.and(
                                        Filters.eq("readACL", new ArrayList<String>()),
                                        Filters.eq("writeACL", new ArrayList<String>())
                                )
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
            return save(user, dashboard);
        }
        return update(user, dashboard);
    }
    public CompletableFuture<Dashboard> save(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);

                dashboard.getReadACL().add(user.getId().toString());
                dashboard.getWriteACL().add(user.getId().toString());

                InsertOneResult result = collection.insertOne(dashboard);
                if (!result.wasAcknowledged() || result.getInsertedId() == null) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not insert data!")));
                }

//                return collection.find(Filters.eq("_id", result.getInsertedId())).first();
                return dashboard;
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

                FindIterable<Dashboard> findResult = collection.find(Filters.eq("_id", dashboard.getId()));
                Dashboard foundDashboard = findResult.first();
                if (foundDashboard == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!ServiceUtils.hasReadWriteAccess(user, foundDashboard)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }
                dashboard.getReadACL().addAll(foundDashboard.getReadACL());
                dashboard.getWriteACL().addAll(foundDashboard.getWriteACL());

                collection.replaceOne(Filters.eq("_id", dashboard.getId()), dashboard);
//                if (!result.wasAcknowledged() || result.getMatchedCount() == 0) {
//                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not update data!")));
//                }

                return dashboard;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw new CompletionException(ex);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    public CompletableFuture<Dashboard> delete(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);

                FindIterable<Dashboard> findResult = collection.find(Filters.eq("_id", dashboard.getId()));
                Dashboard foundDashboard = findResult.first();
                if (foundDashboard == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!ServiceUtils.hasReadWriteAccess(user, dashboard)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }

                collection.deleteOne(Filters.eq("_id", dashboard.getId()));
//                if (!result.wasAcknowledged() || result.getDeletedCount() == 0) {
//                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Could not delete data!")));
//                }

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
