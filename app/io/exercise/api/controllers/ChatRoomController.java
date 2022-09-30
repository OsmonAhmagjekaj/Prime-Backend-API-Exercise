package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.inject.Inject;
import com.mongodb.MongoException;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * ChatRoomController contains methods used to create a WebSocket for chatting.
 * Created by Osmon on 15/08/2022
 */
public class ChatRoomController extends Controller {

    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    @Inject
    private ActorSystem actorSystem;

    @Inject
    private Materializer materializer;

    /**
     * Create a token for a user
     * @param roomId the id of the room to be joined
     * @param token user token used to authenticate access
     * @return WebSocket of the chat
     * @throws CompletionException in case data is not found or an internal error occurred
     * @see io.exercise.api.services.AuthenticateService
     */
    public WebSocket chat (String roomId, String token) {
        return WebSocket.Text.acceptOrResult(request -> {
           try {
               User user = ServiceUtils
                       .extractIdFrom(token)
                       .thenCompose((id) -> ServiceUtils.getUserFrom(mongoDB, id))
                       .thenCompose((usr) -> ServiceUtils.verifyTokenFor(usr, token, config))
                       .join();

               ChatRoom chatRoom = mongoDB.getMongoDatabase()
                       .getCollection("chatRooms", ChatRoom.class)
                       .find(Filters.eq("_id", new ObjectId(roomId)))
                       .first();
               if (chatRoom == null) {
                   throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, "Room not found"));
               }

               if (!(ServiceUtils.containElementsInCommon(chatRoom.getReadACL(), user.getAccessIds())
                       || (chatRoom.getReadACL().size() == 0 && chatRoom.getWriteACL().size() == 0))) {
                   throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, "You cannot join this room!"));
               }

               return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef((out) -> ChatActor.props(out, chatRoom, user), actorSystem, materializer)));
           } catch (JWTVerificationException ex) {
               ex.printStackTrace();
               throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid signature/claims.")));
           } catch (CompletionException ex) {
               ex.printStackTrace();
               throw ex;
           } catch (MongoException ex) {
               ex.printStackTrace();
               throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Mongo error " + ex));
           } catch (Exception ex) {
               ex.printStackTrace();
               throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Invalid")));
           }
        });
    }
}
