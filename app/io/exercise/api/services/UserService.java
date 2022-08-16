package io.exercise.api.services;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.dashboard.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A service class that handles CRUD operations with data from a mongo database.
 * Created by Osmon on 15/08/2022
 */
@Singleton
public class UserService {
    @Inject
    HttpExecutionContext ec;

    @Inject
    IMongoDB mongoDB;

    /**
     * Sets the database up with some users
     * @param users the users to be added to the database
     * @return list of users
     * @throws CompletionException if the data could not be inserted
     * @see io.exercise.api.controllers.UserController
     */
    public CompletableFuture<List<User>> setup (List<User> users) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                // Check if users with such id's exist, if not than populate the database
                users.forEach(user -> {
                    FindIterable<User> found = collection.find(Filters.eq("_id", user.getId()));
                    if (!found.iterator().hasNext()) {
                        collection.insertOne(user);
                    }
                });

                return users;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Could not insert data!" + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    /**
     * Return a list with all the users in the database
     * @return list of users
     * @throws CompletionException if the data could not be fetched
     * @see io.exercise.api.controllers.UserController
     */
    public CompletableFuture<List<User>> all (int skip, int limit, User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection("users", User.class)
                        .find(ServiceUtils.getReadAccessFilterFor(user.getAccessIds()))
                        .skip(skip)
                        .limit(limit)
                        .into(new ArrayList<>());
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Could not fetch data!" + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    /**
     * Save a user into the database
     * @param user to be saved
     * @return the saved user
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<User> save(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

//                user.getReadACL().add(authUser.getId().toString());
//                user.getWriteACL().add(authUser.getId().toString());
                user.setPassword(Hash.createPassword(user.getPassword()));
                collection.insertOne(user);

                return user;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Could not insert data!" + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }

    /**
     * Update a user in the database
     * @param user to be updated
     * @param id of the user
     * @param authUser used for authentication
     * @return the updated user
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<User> update(User user, String id, User authUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                if(Strings.isNullOrEmpty(id) || !ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Incorrect or missing id!")));
                }

                FindIterable<User> findResult = collection.find(Filters.eq("_id", user.getId()));
                User foundUser = findResult.first();
                if (foundUser == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!authUser.hasReadWriteAccessFor(user)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }
                user.getReadACL().addAll(foundUser.getReadACL());
                user.getWriteACL().addAll(foundUser.getWriteACL());
                collection.replaceOne(Filters.eq("_id", new ObjectId(id)), user);

                return user;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Could not update data!" + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }

        }, ec.current());
    }

    /**
     * Delete a user from the database
     * @param user to be deleted
     * @param id of the user
     * @param authUser used for authentication
     * @return the deleted user
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.controllers.DashboardController
     */
    public CompletableFuture<User> delete(User user, String id, User authUser) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                if(Strings.isNullOrEmpty(id) || !ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Incorrect or missing id!")));
                }

                FindIterable<User> findResult = collection.find(Filters.eq("_id", user.getId()));
                User foundUser = findResult.first();
                if (foundUser == null) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Could not find data!")));
                }

                if (!authUser.hasReadWriteAccessFor(user)) {
                    throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("FORBIDDEN!")));
                }
                collection.deleteOne(Filters.eq("_id", new ObjectId(id)));

                return user;
            } catch (CompletionException ex) {
                ex.printStackTrace();
                throw ex;
            } catch (MongoException ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Could not delete data!" + ex));
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, ex));
            }
        }, ec.current());
    }
}