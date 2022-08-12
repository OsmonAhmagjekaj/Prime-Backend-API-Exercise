package io.exercise.api.actors;
import akka.actor.AbstractActor;
import com.typesafe.config.Config;

import javax.inject.Inject;

public class ConfiguredActor extends AbstractActor {

	private Config configuration;

	@Inject
	public ConfiguredActor(Config configuration) {
		this.configuration = configuration;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(ConfiguredActorProtocol.GetConfig.class, message -> {
				sender().tell(configuration.getString("environment"), self());
			})
			.build();
	}
}