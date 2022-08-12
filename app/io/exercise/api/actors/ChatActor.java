package io.exercise.api.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import io.exercise.api.models.ChatRoom;
import io.exercise.api.models.User;

public class ChatActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private static final String JOINED_ROOM = "Joined The Room";
    private static final String LEFT_ROOM = "Left The Room";
    private static final String PING = "PING";
    private static final String PONG = "PONG";

    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private ActorRef out;
    private ChatRoom room;
    private User user;

    public static Props props (ActorRef out, ChatRoom room, User user) {
        return Props.create(ChatActor.class, () -> new ChatActor(out, room, user));
    }

    public ChatActor (ActorRef out, ChatRoom room, User user) {
        this.out = out;
        this.room = room;
        this.user = user;
        mediator.tell(new DistributedPubSubMediator.Subscribe(room.getId().toString(), getSelf()), getSelf());
    }

    @Override
    public Receive createReceive () {
        return receiveBuilder()
                .match(String.class, this::onMessageReceived)
                .match(ChatActorProtocol.ChatMessage.class, this::onChatMessageReceived)
                .match(DistributedPubSubMediator.SubscribeAck.class, this::onSubscribe)
                .match(DistributedPubSubMediator.UnsubscribeAck.class, this::onUnsubscribe)
                .build();
    }

    public void onMessageReceived (String message) {
        if (message.equals(PING)) {
            out.tell(PONG, getSelf());
            return;
        }
        broadcast(message);
    }

    public void onChatMessageReceived (ChatActorProtocol.ChatMessage what) {
        if (getSender().equals(getSelf())) {
            return;
        }
        String message = what.getMessage();
        out.tell(message, getSelf());
    }

    public void onSubscribe (DistributedPubSubMediator.SubscribeAck message) {
        this.joinTheRoom();
    }

    public void onUnsubscribe (DistributedPubSubMediator.UnsubscribeAck message) {
        this.leaveTheRoom();
    }

    @Override
    public void postStop() {
        this.leaveTheRoom();
    }

    private void joinTheRoom () {
        this.broadcast(String.format("%s %s", user.getUsername(), JOINED_ROOM));
    }

    private void leaveTheRoom () {
        this.broadcast(String.format("%s %s", user.getUsername(), LEFT_ROOM));
    }

    private void broadcast (String message) {
        if (!user.hasReadWriteAccessFor(room)) {
            out.tell("You have no access to send messages in this room!", getSelf());
            return;
        }

        mediator.tell(
                new DistributedPubSubMediator.Publish(room.getId().toString(), new ChatActorProtocol.ChatMessage(String.format("%s: %s", user.getUsername(), message))),
                getSelf()
        );
    }
}
