package io.exercise.api.mongo;

import akka.actor.CoordinatedShutdown;
import com.google.inject.Inject;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

public final class InMemoryMongoDB extends MongoDriver {
	private static MongodExecutable mongoEx;
	private static MongodProcess mongoProcess;

	@Inject
	public InMemoryMongoDB(CoordinatedShutdown coordinatedShutdown, Config config) {
		super(coordinatedShutdown, config);
	}

	private void startMongoExecutable() {
		if (mongoEx != null) {
			return;
		}
		try {
			IRuntimeConfig builder = new RuntimeConfigBuilder()
					.defaults(Command.MongoD)
					.processOutput(ProcessOutput.getDefaultInstanceSilent())
					.build();
			MongodStarter starter = MongodStarter.getInstance(builder);
			mongoEx = starter.prepare(new MongodConfigBuilder()
					.version(Version.Main.PRODUCTION)
					.net(new Net("localhost", 12345, Network.localhostIsIPv6()))
					.build());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startMongoProcess() {
		if (mongoProcess != null) {
			return;
		}
		this.startMongoExecutable();
		try {
			mongoProcess = mongoEx.start();
		} catch (IOException|NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Override
	public MongoDatabase connect() {
		this.startMongoProcess();
		client = MongoClients.create("mongodb://localhost:12345");
		return client.getDatabase("test");
	}

	@Override
	public void disconnect() {
		this.stopMongoClient();
		this.stopMongoProcess();
		this.stopMongoExecutable();
	}

	public void stopMongoExecutable() {
		if (mongoEx == null) {
			return;
		}
		mongoEx.stop();
		mongoEx = null;
	}

	public void stopMongoProcess() {
		if (mongoProcess == null) {
			return;
		}
		if (!mongoProcess.isProcessRunning()) {
			return;
		}
		mongoProcess.stop();
		mongoProcess = null;
	}

	public void stopMongoClient() {
		if (client == null) {
			return;
		}
		client.close();
	}
}
