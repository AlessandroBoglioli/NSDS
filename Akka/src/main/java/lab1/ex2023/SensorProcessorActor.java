package lab1.ex2023;

import java.util.LinkedList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.Props;
import lab1.ex2023.messages.TemperatureMsg;

public class SensorProcessorActor extends AbstractActor {

	private List<Integer> readings;
	private double currentAverage;

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TemperatureMsg.class, this::gotData)
				.build();
	}

	private void gotData(TemperatureMsg msg) throws Exception {
		
		System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender());

		if (msg.getTemperature()<0) {
			System.out.println("SENSOR PROCESSOR " + self() + ": Failing!");
			throw new Exception("Actor fault!"); 
		}
		
		readings.add(msg.getTemperature());

		//Todo: change mean computation

		int sum = 0;
		for (Integer i : readings) {
			sum = sum + i;
		}
		currentAverage = sum / (double)readings.size();
		System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
	}

	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}

	public SensorProcessorActor() {
		this.readings = new LinkedList<Integer>();
	}
}
