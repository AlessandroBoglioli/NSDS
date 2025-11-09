package lab1.ex2022;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lab1.ex2022.messsages.NotifyMsg;
import lab1.ex2022.messsages.PublishMsg;
import lab1.ex2022.messsages.SubscribeMsg;

import java.util.HashMap;

public class WorkerActor extends AbstractActor {

    private HashMap<String, ActorRef> subscribers;

    public WorkerActor() {
        subscribers = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(SubscribeMsg.class, this::onSubscribe)
                .match(PublishMsg.class, this::onPublish)
                .build();
    }

    private void onSubscribe(SubscribeMsg msg) {
        subscribers.put(msg.getTopic(), msg.getSender());
    }

    private void onPublish(PublishMsg msg) throws Exception {
        if (subscribers.containsKey(msg.getTopic())) {
            subscribers.get(msg.getTopic()).tell(new NotifyMsg(msg.getValue()), getSelf());
        } else
            throw new Exception("No valid topic");
    }

    public static Props props() {
        return Props.create(WorkerActor.class);
    }
}
