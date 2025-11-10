package lab.ex5;

import akka.actor.AbstractActor;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;

import java.time.Duration;

public class ServerSupervisorActor extends AbstractActor {

    private static final SupervisorStrategy strategy =
        new OneForOneStrategy(
            1,
            Duration.ofMinutes(1),
            DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.restart()) // resume/restart strategy
            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(
                Props.class,
                props -> {
                    getSender().tell(getContext().actorOf(props), getSelf());
                })
            .build();
    }

    public static Props props() {
        return Props.create(ServerSupervisorActor.class);
    }

}
