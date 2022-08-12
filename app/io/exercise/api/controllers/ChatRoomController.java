package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.google.inject.Inject;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.ChatRoomService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ChatRoomController extends Controller {

    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    @Inject
    SerializationService serializationService;

    @Inject
    ChatRoomService service;

    @Inject
    private ActorSystem actorSystem;

    @Inject
    private Materializer materializer;

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
           } catch (Exception ex) {
               ex.printStackTrace();
               throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Invalid")));
           }
        });
    }
}
