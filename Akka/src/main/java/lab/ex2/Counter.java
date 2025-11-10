package lab.ex2;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Counter {

	private static final int INCREMENT_OP = 1;
	private static final int DECREMENT_OP = -1;

	private static final int numThreads = 10;
	private static final int numMessages = 100;

	public static void main(String[] args) {

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef counter = sys.actorOf(CounterActor.props(), "counter");

		final ExecutorService exec = Executors.newFixedThreadPool(numThreads);

		for (int i = 0; i < numMessages; i++) {
			exec.submit(() -> counter.tell(new SimpleMessage(), ActorRef.noSender()));
			exec.submit(() -> counter.tell(new OtherMessage(), ActorRef.noSender()));
		}
		
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		exec.shutdown();
		sys.terminate();

	}

}
