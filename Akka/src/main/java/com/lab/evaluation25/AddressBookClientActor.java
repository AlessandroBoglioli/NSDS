package com.lab.evaluation25;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;

public class AddressBookClientActor extends AbstractActor {

	private ActorRef balancer;

	private Duration timeout = Duration.create(10, SECONDS);

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::onConfig)
				.match(PutMsg.class, this::putEntry)
				.match(GetMsg.class, this::query)
				.build();
	}

	private void onConfig(ConfigMsg msg) {
		balancer = msg.getActorRef();
	}

	void putEntry(PutMsg msg) {
		System.out.println("CLIENT: Sending new entry " + msg.getName() + " - " + msg.getEmail());
		balancer.tell(msg, getSelf());
	}

	void query(GetMsg msg) {
		System.out.println("CLIENT: Issuing query for " + msg.getName());

		ResponseMsg reply = null;
		try {
			Future<Object> waitingForReply = ask(balancer, msg, 10000);
			reply = (ResponseMsg) waitingForReply.result(timeout, null);
		} catch (TimeoutException | InterruptedException e1) {
			e1.printStackTrace();
		}

		if (reply instanceof ReplyMsg ){
			if (((ReplyMsg) reply).getEmail().equals("Not found"))
				System.out.println("CLIENT: Received reply, no email found!");
			else
				System.out.println("CLIENT: Received reply!");
		} else if (reply instanceof TimeoutMsg) {
			System.out.println("CLIENT: Received timeout, both copies are resting!");
		} else {
			System.out.println("Unhandled exception"); // Optional
		}

	}
	static Props props() {
		return Props.create(AddressBookClientActor.class);
	}

}
