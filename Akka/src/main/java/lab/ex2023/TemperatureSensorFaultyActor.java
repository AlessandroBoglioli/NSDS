package lab.ex2023;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lab.ex2023.messages.ConfigMsg;
import lab.ex2023.messages.GenerateMsg;
import lab.ex2023.messages.TemperatureMsg;

public class TemperatureSensorFaultyActor extends TemperatureSensorActor {

	private ActorRef dispatcher;
	private final static int FAULT_TEMP = -50;

	@Override
	public AbstractActor.Receive createReceive() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::configure)
				.match(GenerateMsg.class, this::onGenerate)
				.build();
	}

	private void configure(ConfigMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Received configuration message!");
		this.dispatcher = msg.getDispatcher();
	}

	private void onGenerate(GenerateMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Sensing temperature!");
		dispatcher.tell(new TemperatureMsg(FAULT_TEMP,self()), self());
	}

	static Props props() {
		return Props.create(TemperatureSensorFaultyActor.class);
	}

}
