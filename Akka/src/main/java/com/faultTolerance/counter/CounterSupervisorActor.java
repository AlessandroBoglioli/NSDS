package com.faultTolerance.counter;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import java.time.Duration;

public class CounterSupervisorActor extends AbstractActor {

	 // #strategy
    private static final SupervisorStrategy strategy =
        new OneForOneStrategy(
            1, // Max no of retries (We have only one actor to supervision)
            Duration.ofMinutes(1), // Within what time period
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()) // Matching clause and action
                .build());

	// In the strategy we can also we can also resume the operations since this is a false fault
	// we can also stop the actor, but it will not execute the next normal operation

    @Override
    public SupervisorStrategy supervisorStrategy() {
      	return strategy;
    }

	public CounterSupervisorActor() {
	}

	@Override
	public Receive createReceive() {
		// Creates the child actor within the supervisor actor context
		return receiveBuilder()
		          .match(
		              Props.class,
		              props -> {
		                	getSender().tell(getContext().actorOf(props), getSelf());
		              })
		          .build();
	}

	public static Props props() {
		return Props.create(CounterSupervisorActor.class);
	}

}
