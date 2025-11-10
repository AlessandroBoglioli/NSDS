package lab1.ex2023;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import lab1.ex2023.messages.DispatchLogicMsg;
import lab1.ex2023.messages.TemperatureMsg;

public class DispatcherActor extends AbstractActor {

	private Map<ActorRef, ActorRef> dispatchMap;
	private Map<ActorRef, Integer> processorLoad;

	private Iterator<ActorRef> nextProcessor;

	private final static int NO_PROCESSORS = 2;

	private static SupervisorStrategy strategy =
			new OneForOneStrategy(
					1,
					Duration.ofMinutes(1),
					DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume()).build());

	public DispatcherActor() {
		dispatchMap = new HashMap<ActorRef, ActorRef>();
		processorLoad = new HashMap<ActorRef, Integer>();
		for (int i = 0; i < NO_PROCESSORS; i++) {
			processorLoad.put(getContext().actorOf(SensorProcessorActor.props()), 0);
		}
		nextProcessor = processorLoad.keySet().iterator();
	}

	@Override
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}

	@Override
	public Receive createReceive() {
		return roundRobin();	// starting with round robin
	}

	private Receive loadBalancer() {
		return receiveBuilder()
				.match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
				.match(DispatchLogicMsg.class, this::changeDispatcherLogic)
				.build();
	}

	private Receive roundRobin() {
		return receiveBuilder()
				.match(TemperatureMsg.class, this::dispatchDataRoundRobin)
				.match(DispatchLogicMsg.class, this::changeDispatcherLogic)
				.build();
	}

	private void changeDispatcherLogic(DispatchLogicMsg msg) {
		if (msg.getLogic() == DispatchLogicMsg.LOAD_BALANCER) {
			System.out.println("DISPATCHER: Switching to load balancer!");
			getContext().become(loadBalancer());
		} else {
			System.out.println("DISPATCHER: Switching to round robin!");
			getContext().become(roundRobin());
		}
	}

	private ActorRef findLowLoadProcessor() {

		// Finding the lowest load
		ActorRef lowLoadProcessor = null;
		int lowLoad = Integer.MAX_VALUE;
		for (ActorRef p : processorLoad.keySet()) {
			if (processorLoad.get(p) < lowLoad) {
				lowLoadProcessor = p;
				lowLoad = processorLoad.get(p);
			}
		}

		return lowLoadProcessor;
	}

	private void dispatchDataLoadBalancer(TemperatureMsg msg) {

		ActorRef targetProcessor = null;
		if (!dispatchMap.keySet().contains(msg.getSender())) {
			targetProcessor = findLowLoadProcessor();
			processorLoad.put(targetProcessor, processorLoad.get(targetProcessor) + 1);
			dispatchMap.put(msg.getSender(), targetProcessor);
		}
		dispatchMap.get(msg.getSender()).tell(msg, self());
	}

	private void dispatchDataRoundRobin(TemperatureMsg msg) {

		if (!nextProcessor.hasNext()) {
			nextProcessor = processorLoad.keySet().iterator();
		}
		nextProcessor.next().tell(msg, self());
	}

	static Props props() {
		return Props.create(DispatcherActor.class);
	}
}
