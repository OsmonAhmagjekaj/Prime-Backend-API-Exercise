package io.exercise.api.modules;

import com.google.inject.AbstractModule;
import io.exercise.api.actors.ConfiguredActor;
import play.libs.akka.AkkaGuiceSupport;

public class ActorModule extends AbstractModule implements AkkaGuiceSupport {

    @Override
    protected void configure() {
        bindActor(ConfiguredActor.class, "configured-actor");
    }
}
