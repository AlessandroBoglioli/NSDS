package com.counter;

import akka.actor.AbstractActor;
import akka.actor.Props;

import javax.print.attribute.standard.MediaSize;

public class CounterActor extends AbstractActor {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	// Now the actor knows what to do with the new message type

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage)
				.match(OtherMessage.class, this::onOtherMessage)
				.build();
	}

	// Method to handle the OtherMessage type

	private void onOtherMessage(OtherMessage otherMessage) {
		--counter;
		System.out.println("Counter increased to " + counter);
	}

	void onMessage(SimpleMessage msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
