package lab1.ex2022;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.faultTolerance.counter.CounterActor;
import com.faultTolerance.counter.CounterSupervisorActor;
import lab1.ex2022.messsages.BatchMsg;
import lab1.ex2022.messsages.ConfigMsg;
import lab1.ex2022.messsages.GeneratePubMsg;
import lab1.ex2022.messsages.GenerateSubMsg;
import scala.concurrent.duration.Duration;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class PubSub {

	public final static String TOPIC0 = "topic0";
	public final static String TOPIC1 = "topic1";

	public static void main(String[] args) throws InterruptedException, TimeoutException {

		scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

		final ActorSystem sys = ActorSystem.create("System");
		final ActorRef broker = sys.actorOf(BrokerActor.props(), "broker");
		final ActorRef subscriber = sys.actorOf(SubscriberActor.props(), "subscriber");
		final ActorRef publisher = sys.actorOf(PublisherActor.props(), "publisher");

		subscriber.tell(new ConfigMsg(broker), ActorRef.noSender());
		publisher.tell(new ConfigMsg(broker), ActorRef.noSender());

		ActorRef worker1;
		ActorRef worker2;

		scala.concurrent.Future<Object> waitingForCounter = ask(broker, Props.create(WorkerActor.class), 5000);
		worker1 = (ActorRef) waitingForCounter.result(timeout, null);
		scala.concurrent.Future<Object> waitingForCounter1 = ask(broker, Props.create(WorkerActor.class), 5000);
		worker2 = (ActorRef) waitingForCounter1.result(timeout, null);

		// Some example subscriptions
		subscriber.tell(new GenerateSubMsg(TOPIC0), ActorRef.noSender());
		subscriber.tell(new GenerateSubMsg(TOPIC1), ActorRef.noSender());
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// Some example events		
		publisher.tell(new GeneratePubMsg(TOPIC0, "Test event 1"), ActorRef.noSender());
		publisher.tell(new GeneratePubMsg(TOPIC1, "Test event 2"), ActorRef.noSender());
		
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Turn the broker in batching mode
		broker.tell(new BatchMsg(true), ActorRef.noSender());

		// Waiting for messages to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// More example events
		publisher.tell(new GeneratePubMsg(TOPIC0, "Test message 3"), ActorRef.noSender());
		publisher.tell(new GeneratePubMsg(TOPIC1, "Test message 4"), ActorRef.noSender());
		
		// Waiting for events to propagate
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		broker.tell(new BatchMsg(false), ActorRef.noSender());
		// In this example, the last two events shall not be processed until after this point

		// Wait for all messages to be sent and received
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sys.terminate();
	}

}
