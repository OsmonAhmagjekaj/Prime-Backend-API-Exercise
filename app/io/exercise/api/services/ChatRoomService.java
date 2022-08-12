package io.exercise.api.services;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.actors.ChatActorProtocol;
import play.libs.concurrent.HttpExecutionContext;
import play.libs.streams.ActorFlow;
import play.mvc.WebSocket;

import java.util.concurrent.CompletableFuture;

public class ChatRoomService {

    @Inject
    HttpExecutionContext ec;

    @Inject
    private ActorSystem actorSystem;

    public CompletableFuture<JsonNode> publish (JsonNode node, String room) {
        return CompletableFuture.supplyAsync(() -> {
            Cluster cluster = Cluster.get(actorSystem);
            ActorRef mediator = DistributedPubSub.get(cluster.system()).mediator();
            String message = "User: " + node.get("text").asText();
            mediator.tell(
                    new DistributedPubSubMediator.Publish(room, new ChatActorProtocol.ChatMessage(message)),
                    ActorRef.noSender()
            );

            return node;
        }, ec.current());
    }
}
