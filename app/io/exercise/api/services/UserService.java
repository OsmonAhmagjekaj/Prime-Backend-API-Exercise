package io.exercise.api.services;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
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
 * Created by Osmon on 2/08/2022
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
    public CompletableFuture<List<User>> all () {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mongoDB.getMongoDatabase()
                        .getCollection("users", User.class)
                        .find()
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
     * Save/add a user in the database
     * @param user the user to be added
     * @return the added user
     * @throws CompletionException if the user doesn't exist or the data could not be inserted
     * @see io.exercise.api.controllers.UserController
     */
    public CompletableFuture<User> save(User user) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                // We could also check if the usernames match using Filters.or(Filters.eq("_id", user.getId(), Filters.eq("username", user.getUsername())
                FindIterable<User> found = collection.find(Filters.eq("_id", user.getId()));
                if (found.iterator().hasNext()) {
                    throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("User already exists!")));
                }

                user.setPassword(Hash.createPassword(user.getPassword()));
                collection.insertOne(user);
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

            return user;
        }, ec.current());
    }

    /**
     * Update a user in the database
     * @param user the user to be updated
     * @param id the id of the user
     * @return the updated user
     * @throws CompletionException in case of missing or incorrect id, user doesn't exist or data could not be inserted
     * @see io.exercise.api.controllers.UserController
     */
    public CompletableFuture<User> update(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                if(Strings.isNullOrEmpty(id) || !ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Incorrect or missing id!")));
                }

                collection.replaceOne(Filters.eq("_id", new ObjectId(id)), user);
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

            return user;
        }, ec.current());
    }

    /**
     * Delete a user in the database
     * @param user the user to be deleted
     * @param id the id of the user
     * @return the deleted user
     * @throws CompletionException in case of missing or incorrect id, user doesn't exist or data could not be inserted
     * @see io.exercise.api.controllers.UserController
     */
    public CompletableFuture<User> delete(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MongoCollection<User> collection = mongoDB.getMongoDatabase()
                        .getCollection("users", User.class);

                if(Strings.isNullOrEmpty(id) || !ObjectId.isValid(id)) {
                    throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Incorrect or missing id!")));
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