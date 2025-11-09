package lab1.ex2023;

import akka.actor.AbstractActorWithStash;
import akka.actor.ActorRef;
import akka.actor.Props;
import lab1.ex2023.messages.ConfigMsg;
import lab1.ex2023.messages.DispatchLogicMsg;
import lab1.ex2023.messages.TemperatureMsg;

import java.util.ArrayList;

public class DispatcherActor extends AbstractActorWithStash {

	private final static int NO_PROCESSORS = 2;
	private ArrayList<ActorRef> sensors;

	public DispatcherActor() {
		sensors = new ArrayList<>(NO_PROCESSORS);
	}

	@Override
	public Receive createReceive() {
		return roundRobin();
	}

	private Receive roundRobin() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::onConfig)
				.match(DispatchLogicMsg.class, this::onloadBalancing)
				.match(TemperatureMsg.class, this::dispatchDataRoundRobin)
				.build();
	}

	private Receive loadBalancing() {
		return receiveBuilder()
				.match(ConfigMsg.class, this::onConfig)
				.match(DispatchLogicMsg.class, this::onRoundRobin)
				.match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
				.build();
	}

	private void onloadBalancing(DispatchLogicMsg msg) {
		getContext().become(loadBalancing());
	}

	private void onRoundRobin(DispatchLogicMsg msg) {
		getContext().become(roundRobin());
	}

	private void onConfig(ConfigMsg msg) {
		sensors.add(msg.getActor());
	}

	private void dispatchDataLoadBalancer(TemperatureMsg msg) {



	}

	private void dispatchDataRoundRobin(TemperatureMsg msg) {



	}

	static Props props() {
		return Props.create(DispatcherActor.class);
	}
}
