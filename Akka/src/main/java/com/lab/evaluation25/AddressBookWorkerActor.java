package com.lab.evaluation25;

import java.util.HashMap;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class AddressBookWorkerActor extends AbstractActor {

	private HashMap<String, String> primaryAddresses;
	private HashMap<String, String> replicaAddresses;

	public AddressBookWorkerActor() {
		this.primaryAddresses = new HashMap<String, String>();
		this.replicaAddresses = new HashMap<String, String>();
	}

	@Override
	public Receive createReceive() {
		return active();
	}

	private Receive active() {
		return receiveBuilder()
				.match(StoreMsg.class, this::onStore)
				.match(GetMsg.class, this::generateReply)
				.match(RestMsg.class, this::sleep)
				.build();
	}

	private void onStore(StoreMsg msg) {
		if (msg.getPrimary())
			primaryAddresses.put(msg.getName(), msg.getEmail());
		else
			replicaAddresses.put(msg.getName(), msg.getEmail());
	}

	private Receive sleeping() {
		return receiveBuilder()
				.match(GetMsg.class, GetMsg -> {} )
				.match(StoreMsg.class, storeMsg -> {} )
				.match(ResumeMsg.class, this::wakeup)
				.build();
	}

	private void wakeup(ResumeMsg msg) {
		getContext().become(active());
	}

	private void sleep(RestMsg msg) {
		getContext().become(sleeping());
	}
	
	void generateReply(GetMsg msg) {
		System.out.println(this.toString() + ": Received query for name " + msg.getName());

		String email;

		if (this.primaryAddresses.containsKey(msg.getName())){
			email = this.primaryAddresses.get(msg.getName());
		} else if (this.replicaAddresses.containsKey(msg.getName())) {
			email = this.replicaAddresses.get(msg.getName());
		} else {
			email = "Not found";
		}

		getSender().tell(new ReplyMsg(email), self());

	}

	static Props props() {
		return Props.create(AddressBookWorkerActor.class);
	}
}
