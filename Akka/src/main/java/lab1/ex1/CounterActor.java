package lab1.ex1;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class CounterActor extends AbstractActor {

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
		--counter;
		System.out.println("Counter increased to " + counter);
	}

	void onMessage(SimpleMessage msg) {
		++counter;
		System.out.println("Counter increased to " + counter);
	}

	void onModifyMessage(ModifyMessage message){
		counter += message.getCode();
		System.out.println("Counter increased to " + counter);
	}

	static Props props() {
		return Props.create(CounterActor.class);
	}

}
