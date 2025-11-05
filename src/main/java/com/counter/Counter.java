package com.counter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Counter {

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		// Send messages from multiple threads in parallel
		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		// ActorRef.noSender identifies a symbolic sender that is outside the actor system
		// We don't want to express the reference of the sender

		for (int i = 0; i < numMessages; i++) {
			exec.submit(() -> counter.tell(new SimpleMessage(), ActorRef.noSender()));

			// We will send also the other typer of messages
			// With no actor method to handle the message it will not be considered

			// After the definition we can se that the reciving of the messages is intervaleted
			exec.submit(() -> counter.tell(new OtherMessage(), ActorRef.noSender()));
		}
		
		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exec.shutdown();
		sys.terminate();

	}

}
