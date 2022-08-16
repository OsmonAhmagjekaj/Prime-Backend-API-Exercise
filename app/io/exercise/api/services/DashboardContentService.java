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

/**
 *  DashboardContentService contains service methods for DashboardContentController.
 * Created by Osmon on 15/08/2022
 */
public class DashboardContentService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    /**
     * Get a list of all the dashboard contents
     * @param skip number of dashboard contents to skip per page
     * @param limit number of dashboard contents to limit per page
     * @param user used for authentication
     * @return result containing all dashboard contents
     * @throws CompletionException in case data is not found or an internal error occurred
     * @throws MongoException in case mongo operations fail
     * @see io.exercise.api.controllers.DashboardContentController
     */
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

    /**
     * Save a dashboard content into the database
     * @param user used for authentication
     * @param content to be saved
     * @param id of the parent dashboard
     * @return the saved dashboard content
     * @throws CompletionException in case data is not found or an internal error occurred
     * @throws MongoException in case mongo operations fail
     * @see io.exercise.api.controllers.DashboardContentController
     */
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

    /**
     * Update a dashboard content in the database
     * @param user used for authentication
     * @param content to be updated
     * @param id of the parent dashboard
     * @return the updated dashboard content
     * @throws CompletionException in case data is not found or an internal error occurred
     * @throws MongoException in case mongo operations fail
     * @see io.exercise.api.controllers.DashboardContentController
     */
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

    /**
     * Delete a dashboard content from the database
     * @param user used for authentication
     * @param content to be deleted
     * @return the deleted dashboard content
     * @throws CompletionException in case data is not found or an internal error occurred
     * @throws MongoException in case mongo operations fail
     * @see io.exercise.api.controllers.DashboardController
     */
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
