package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Content;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
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

    public CompletableFuture<List<Content>> all (int skip, int limit, User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class)
                        .find(Filters.and(
                                Filters.eq("dashboardId", new ObjectId(id)),
                                ServiceUtils.getReadAccessFilterFor(user.getAccessIds())
                        ))
                        .skip(skip)
                        .limit(limit)
                        .into(new ArrayList<>());
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    public CompletableFuture<Content> save(User user, Content content, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dashboard dashboard = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .find(Filters.eq("id", new ObjectId(id)))
                        .first();
                if (dashboard == null) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "No dashboard exists with such id!"));
                }

                MongoCollection<Content> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);

                content.setDashboardId(new ObjectId(id));
                content.getReadACL().add(user.getId().toString());
                content.getWriteACL().add(user.getId().toString());
                collection.insertOne(content);

                return content;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    public CompletableFuture<Content> update(User user, Content content, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Dashboard dashboard = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class)
                        .find(Filters.eq("id", new ObjectId(id)))
                        .first();
                if (dashboard == null) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, "No dashboard exists with such id!"));
                }

                MongoCollection<Content> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);
                content.setDashboardId(new ObjectId(id));

                FindIterable<Content> findResult = collection.find(Filters.eq("_id", content.getId()));
                Content foundContent = findResult.first();
                if (foundContent == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!user.hasReadWriteAccessFor(foundContent)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }
                content.getReadACL().addAll(foundContent.getReadACL());
                content.getWriteACL().addAll(foundContent.getWriteACL());
                collection.replaceOne(Filters.eq("_id", content.getId()), content);

                return content;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    public CompletableFuture<Content> delete(User user, Content content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Content> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);

                FindIterable<Content> findResult = collection.find(Filters.eq("_id", content.getId()));
                Content foundContent = findResult.first();
                if (foundContent == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!user.hasReadWriteAccessFor(foundContent)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }
                collection.deleteOne(Filters.eq("_id", content.getId()));

                return content;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }
}
