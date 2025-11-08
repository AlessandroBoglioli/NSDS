package lab1.ex2023;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lab1.ex2023.messages.TemperatureMsg;

public class SensorProcessorActor extends AbstractActor {

	private double currentAverage;

	@Override
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
	}

	private void gotData(TemperatureMsg msg) throws Exception {

		System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender());

		// ...
		
		System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
	}

	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}

	public SensorProcessorActor() {
	}
}
