package lab1.ex2022;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import lab1.ex2022.messsages.*;

import java.time.Duration;
import java.util.ArrayList;

public class BrokerActor extends AbstractActorWithStash {

    private ArrayList<ActorRef> workers;

    private static final SupervisorStrategy strategy =
            new OneForOneStrategy(
                    2, // Max no of retries
                    Duration.ofMinutes(1), // Within what time period
                    DeciderBuilder.match(Exception.class, e -> SupervisorStrategy.resume()) // Matching clause and action
                            .build());

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public BrokerActor() {
        workers = new ArrayList<>();
    }

    @Override
    public Receive createReceive() {
        return active();
    }

    private Receive active(){
        return receiveBuilder().match(
                Props.class,
                props -> {
                    ActorRef temp = getContext().actorOf(props);
                    workers.add(temp);
                    getSender().tell(temp, getSelf());
                })
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPublish)
                .match(BatchMsg.class, this::onBatch)
                .build();
    }

    private Receive stashing(){
        return receiveBuilder().match(
                Props.class,
                props -> {
                    ActorRef temp = getContext().actorOf(props);
                    workers.add(temp);
                    getSender().tell(temp, getSelf());
                })
                .match(SubscribeMsg.class, this::onStash)
                .match(PublishMsg.class, this::onStash)
                .match(BatchMsg.class, this::onBatch)
                .build();
    }

    private void onSubscribe(SubscribeMsg msg) {
        System.out.println("Hello");
        if (msg.getKey() % 2 == 0)
            workers.get(0).tell(msg, getSelf());
        else
            workers.get(1).tell(msg, getSelf());
    }

    private void onPublish(PublishMsg msg) {
        for (ActorRef worker : workers)
            worker.tell(msg, getSelf());
    }

    private void onStash(Msg msg) {
        stash();
    }

    private void onBatch(BatchMsg msg) {
        if (msg.isOn()){
            getContext().become(stashing());
        } else {
            getContext().become(active());
            unstashAll();
        }
    }

    public static Props props() {
        return Props.create(BrokerActor.class);
    }
}
