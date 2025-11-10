package com.lab.evaluation25;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class AddressBookBalancerActor extends AbstractActor {

	private Duration timeout = Duration.create(1, SECONDS);

	ActorRef worker1 = null;
	ActorRef worker0 = null;

	public AddressBookBalancerActor() {
	}

	@Override
	public Receive createReceive() {
		// TODO: Rewrite next line...
		return receiveBuilder()
				.match(ConfigMsg.class, this::onConfig)
				.match(PutMsg.class, this::storeEntry)
				.match(GetMsg.class, this::routeQuery)
				.build();
	}

	private void onConfig(ConfigMsg msg) {
		if (worker0 == null)
			worker0 = msg.getActorRef();
		else
			worker1 = msg.getActorRef();
	}

	int splitByInitial(String s) {
		char firstChar = s.charAt(0);

		// Normalize case for comparison
		char upper = Character.toUpperCase(firstChar);

		if (upper >= 'A' && upper <= 'M') {
			return 0;
		} else {
			return 1;
		}
	}

	void routeQuery(GetMsg msg) {

		System.out.println("BALANCER: Received query for name " + msg.getName());

		ResponseMsg reply = null;
//		if (splitByInitial(msg.getName()) == 0){
//			try {
//				Future<Object> waitingForReply0 = ask(worker0, msg, 5000);
//				reply = (ResponseMsg) waitingForReply0.result(timeout, null);
//
//				if (reply == null) {
//					System.out.println("BALANCER: Primary copy query for name " + msg.getName() + " is resting!");
//
//					Future<Object> waitingForReply1 = ask(worker1, msg, 5000);
//					reply = (ResponseMsg) waitingForReply1.result(timeout, null);
//
//					if (reply != null) {
//
//						getSender().tell(reply, self());
//					}
//					else {
//						System.out.println("BALANCER: Both copies are resting for name " + msg.getName() + "!");
//
//						getSender().tell(new TimeoutMsg(), self());
//					}
//				}
//				else {
//					getSender().tell(reply, self());
//				}
//			} catch (TimeoutException | InterruptedException e1) {
//				e1.printStackTrace();
//			}
//		}
//		else {
//			try {
//				Future<Object> waitingForReply1 = ask(worker1, msg, 5000);
//				reply = (ResponseMsg) waitingForReply1.result(timeout, null);
//
//				if (reply == null) {
//					System.out.println("BALANCER: Primary copy query for name " + msg.getName() + " is resting!");
//
//					Future<Object> waitingForReply0 = ask(worker0, msg, 5000);
//					reply = (ResponseMsg) waitingForReply0.result(timeout, null);
//
//					if (reply != null) {
//						getSender().tell(reply, self());
//					}
//					else {
//						System.out.println("BALANCER: Both copies are resting for name " + msg.getName() + "!");
//
//						getSender().tell(new TimeoutMsg(), self());
//					}
//				}
//				else {
//					getSender().tell(reply, self());
//				}
//			} catch (TimeoutException | InterruptedException e1) {
//				e1.printStackTrace();
//			}
//		}

		if (splitByInitial(msg.getName()) == 0){
			try {
				Future<Object> waitingForReply0 = ask(worker0, msg, 1000);
				reply = (ResponseMsg) waitingForReply0.result(timeout, null);
				getSender().tell(reply, self());

			} catch (TimeoutException | InterruptedException e1) {

				System.out.println("BALANCER: Primary copy query for name " + msg.getName() + " is resting!");

				try {
					Future<Object> waitingForReply1 = ask(worker1, msg, 1000);
					reply = (ResponseMsg) waitingForReply1.result(timeout, null);
					getSender().tell(reply, self());
				}
				catch (TimeoutException | InterruptedException e2) {
					System.out.println("BALANCER: Both copies are resting for name " + msg.getName() + "!");
					getSender().tell(new TimeoutMsg(), self());
				}
			}
		}
		else {
			try {
				Future<Object> waitingForReply0 = ask(worker1, msg, 1000);
				reply = (ResponseMsg) waitingForReply0.result(timeout, null);
				getSender().tell(reply, self());

			} catch (TimeoutException | InterruptedException e1) {

				System.out.println("BALANCER: Primary copy query for name " + msg.getName() + " is resting!");

				try {
					Future<Object> waitingForReply1 = ask(worker0, msg, 1000);
					reply = (ResponseMsg) waitingForReply1.result(timeout, null);
					getSender().tell(reply, self());
				}
				catch (TimeoutException | InterruptedException e2) {
					System.out.println("BALANCER: Both copies are resting for name " + msg.getName() + "!");
					getSender().tell(new TimeoutMsg(), self());
				}
			}
		}

	}

	void storeEntry(PutMsg msg) {
		System.out.println("BALANCER: Received new entry " + msg.getName() + " - " + msg.getEmail());

		if (splitByInitial(msg.getName()) == 0) {
			worker0.tell(new StoreMsg(msg.getName(), msg.getEmail(), true), getSelf());
			worker1.tell(new StoreMsg(msg.getName(), msg.getEmail(), false), getSelf());
		}
		else {
			worker1.tell(new StoreMsg(msg.getName(), msg.getEmail(), true), getSelf());
			worker0.tell(new StoreMsg(msg.getName(), msg.getEmail(), false), getSelf());
		}

	}

	static Props props() {
		return Props.create(AddressBookBalancerActor.class);
	}

}
