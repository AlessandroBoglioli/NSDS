package Lab1.Ex2;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class CounterActor extends AbstractActorWithStash {

	private int counter;

	public CounterActor() {
		this.counter = 0;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(SimpleMessage.class, this::onMessage)
				.match(OtherMessage.class, this::onOtherMessage)
				.match(ModifyMessage.class, this::onModifyMessage)
				.build();
	}

	private void onOtherMessage(OtherMessage otherMessage) {
		if (counter <= 0){
			this.stash();
		} else {
			--counter;
			System.out.println("Counter increased to " + counter);
		}
	}

	void onMessage(SimpleMessage msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
		this.unstashAll();
	}

	void onModifyMessage(ModifyMessage message){
        if (counter <= 0 && message.getCode() == -1) {
            this.stash();
        } else {
			counter += message.getCode();
			System.out.println("Counter increased to " + counter);
			this.unstashAll();
		}

	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
