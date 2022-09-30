package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.GraphLookupOptions;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Content;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

/**
 * DashboardService contains service methods for DashboardController.
 * Created by Osmon on 15/08/2022
 */
public class DashboardService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    /**
     * Get a list of all the dashboards together with their items
     * @param skip number of dashboards to skip per page
     * @param limit number of dashboards to limit per page
     * @param user used for authentication
     * @return result containing all dashboards
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<List<Dashboard>> all(int skip, int limit, User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return mongoDB.getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class)
                                .find(ServiceUtils.getReadAccessFilterFor(user.getAccessIds()))
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
                }, ec.current()
        ).thenApply(dashboards -> {
            try {
                MongoCollection<Content> contentsCollection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);

                Map<ObjectId, List<Content>> list = contentsCollection.find(
                                Filters.and(
                                        ServiceUtils.getReadAccessFilterFor(user.getAccessIds()),
                                        Filters.in("dashboardId",
                                                dashboards.stream()
                                                        .map(BaseModel::getId)
                                                        .collect(Collectors.toList()))
                                )).into(new ArrayList<>())
                        .stream()
                        .collect(Collectors.groupingBy(Content::getDashboardId));

                return dashboards
                        .stream()
                        .peek(x -> x.setItems(list.get(x.getId())))
                        .collect(Collectors.toList());
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        });
    }

    /**
     * Get a list of all the dashboards in a hierarchial manner, together with their items
     * @param skip number of dashboards to skip per page
     * @param limit number of dashboards to limit per page
     * @param user used for authentication
     * @return result containing all dashboards in a hierarchical manner
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<List<Dashboard>> hierarchy(int skip, int limit, User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> dashboardsCollection = mongoDB.getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);

                        List<Bson> pipeline = new ArrayList<>();

                        pipeline.add(Aggregates.match(
                                ServiceUtils.getReadAccessFilterFor(user.getAccessIds())
                        ));
                        pipeline.add(Aggregates.skip(skip));
                        pipeline.add(Aggregates.limit(limit));

                        // Adding the content items to dashboards using mongo aggregation
//                        pipeline.add(Aggregates.lookup(
//                                "dashboardsContent",
//                                "_id",
//                                "dashboardId",
//                                "items"
//                        ));

                        // Adding the content items to dashboards using mongo aggregation
                        pipeline.add(Aggregates.match(
                                Filters.eq("parentId", null)
                        ));

                        pipeline.add(Aggregates.graphLookup(
                                "dashboards",
                                "$_id",
                                "_id",
                                "parentId",
                                "children"
                        ));

                        return dashboardsCollection
                                .aggregate(pipeline, Dashboard.class)
                                .into(new ArrayList<>());
                    } catch (MongoException ex) {
                        ex.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
                    }
                }, ec.current()
        ).thenApply(dashboards -> {
            try {
                // Adding the content items to dashboards using java
                List<Dashboard> dashboardFlat = dashboards.stream()
                        .map(Dashboard::getChildren)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
                dashboardFlat.addAll(dashboards);

                MongoCollection<Content> contentsCollection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class);

                Map<ObjectId, List<Content>> list = contentsCollection.find(
                                Filters.and(
                                        ServiceUtils.getReadAccessFilterFor(user.getAccessIds()),
                                        Filters.in("dashboardId",
                                                dashboardFlat.stream()
                                                        .map(BaseModel::getId)
                                                        .collect(Collectors.toList()))
                                )).into(new ArrayList<>())
                        .stream()
                        .collect(Collectors.groupingBy(Content::getDashboardId));

                dashboardFlat.forEach(next -> {
                    next.setItems(list.get(next.getId()));
                });

                return dashboards;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }
        ).thenApply(dashboards -> {
            try {
                // Building the dashboards hierarchy
                return dashboards
                        .stream()
                        .map(dashboard -> {
                            try {
                                Dashboard parent = dashboard.clone();
                                parent.setChildren(new ArrayList<>());
                                recursiveHierarchy(dashboard.getChildren(), parent);
                                return parent;
                            } catch (CloneNotSupportedException e) {
                                throw new RuntimeException(e);
                            }
                        }).collect(Collectors.toList());
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        });
    }

    public CompletableFuture<List<Dashboard>> hierarchy2(int skip, int limit, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> dashboardsCollection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsSmall", Dashboard.class);

                List<Bson> pipeline = new ArrayList<>();

                pipeline.add(Aggregates.match(
                        ServiceUtils.getReadAccessFilterFor(user.getAccessIds())
                ));

                pipeline.add(Aggregates.skip(skip));
                pipeline.add(Aggregates.limit(limit));

                pipeline.add(Aggregates.match(Filters.eq("parentId", null)));

                pipeline.add(Aggregates.graphLookup(
                        "dashboards",
                        "$_id",
                        "_id",
                        "parentId",
                        "children"
                ));

                return dashboardsCollection
                        .aggregate(pipeline, Dashboard.class)
                        .into(new ArrayList<>())
                        .stream()
                        .map(dashboard -> {
                            Dashboard parent;
                            try {
                                parent = dashboard.clone();
                                parent.setChildren(new ArrayList<>());
                            } catch (CloneNotSupportedException e) {
                                throw new RuntimeException(e);
                            }

                            recursiveHierarchy(dashboard.getChildren(), parent);
                            return parent;
                        }).collect(Collectors.toList());
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
     * Build a hierarchy of dashboards
     * @param parent parent to start from
     * @param input list of dashboards
     */
    void buildHierarchyTree(Dashboard parent, List<Dashboard> input) {
        List<Dashboard> children = input
                .stream()
                .filter(next -> parent.getId().equals(next.getParentId()))
                .collect(Collectors.toList());

        parent.setChildren(children);
        if (children.size() == 0) {
            return;
        }

        children.forEach(next -> buildHierarchyTree(next, input));
    }

    /**
     * Build a hierarchy of dashboards
     * @param dashboards list of dashboards
     * @param parent parent to start from
     */
    public void recursiveHierarchy(List<Dashboard> dashboards, Dashboard parent) {
        CompletableFuture.supplyAsync(() -> {
            for (Dashboard dashboard : dashboards) {
                if (dashboard.getParentId().equals(parent.getId())) {
                    parent.getChildren().add(dashboard);
                    recursiveHierarchy(dashboards, dashboard);
                }
            }
            return null;
        });
    }

    /**
     * Save a dashboard into the database
     * @param user used for authentication
     * @param dashboard to be saved
     * @return the saved dashboard
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<Dashboard> save(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);

                dashboard.getReadACL().add(user.getId().toString());
                dashboard.getWriteACL().add(user.getId().toString());
                collection.insertOne(dashboard);

                return dashboard;
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
     * Update a dashboard in the database
     * @param user used for authentication
     * @param dashboard to be updated
     * @return the updated dashboard
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
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

                if (!user.hasReadWriteAccessFor(foundDashboard)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }
                dashboard.getReadACL().addAll(foundDashboard.getReadACL());
                dashboard.getWriteACL().addAll(foundDashboard.getWriteACL());
                collection.replaceOne(Filters.eq("_id", dashboard.getId()), dashboard);

                return dashboard;
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
     * Delete a dashboard from the database
     * @param user used for authentication
     * @param dashboard to be deleted
     * @return the deleted dashboard
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<Dashboard> delete(User user, Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<Dashboard> dashboardsCollection = mongoDB.getMongoDatabase()
                        .getCollection("dashboardsSmall", Dashboard.class);

                FindIterable<Dashboard> findResult = dashboardsCollection.find(Filters.eq("_id", dashboard.getId()));
                Dashboard foundDashboard = findResult.first();
                if (foundDashboard == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!user.hasReadWriteAccessFor(foundDashboard)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }

                return dashboardsCollection;
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
        }, ec.current()
        ).thenApply(dashboardsCollection -> {
            try {
                List<Bson> pipeline = new ArrayList<>();
                //pipeline.add(Aggregates.match(ServiceUtils.getWriteAccessFilterFor(user.getAccessIds())));
                pipeline.add(Aggregates.match(Filters.eq("_id", dashboard.getId())));

                pipeline.add(Aggregates.graphLookup(
                        "dashboardsSmall",
                        "$_id",
                        "_id",
                        "parentId",
                        "children",
                        new GraphLookupOptions()
                                .restrictSearchWithMatch(Filters.nin("readACL", user.getAccessIds()))
                ));

                List<Dashboard> dashboards = dashboardsCollection
                        .aggregate(pipeline, Dashboard.class)
                        .into(new ArrayList<>());

                List<ObjectId> dashboardsIds = dashboards.get(0)
                        .getChildren()
                        .stream()
                        //.filter(user::hasReadWriteAccessFor)
                        .map(Dashboard::getId)
                        .collect(Collectors.toList());
                dashboardsIds.add(dashboard.getId());

                mongoDB.getMongoDatabase()
                        .getCollection("dashboardsContent", Content.class)
                        .deleteMany(Filters.in("dashboardId", dashboardsIds));

                dashboardsCollection.deleteMany(Filters.or(
                        Filters.eq("_id", dashboard.getId()),
                        Filters.in("parentId", dashboardsIds)
                ));

                return dashboard;
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
        });
    }
}
