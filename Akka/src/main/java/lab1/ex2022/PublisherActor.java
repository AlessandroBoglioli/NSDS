package lab1.ex2022;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lab1.ex2022.messsages.*;

public class PublisherActor extends AbstractActor {

    private ActorRef broker;

    public PublisherActor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMsg.class, this::onConfig)
                .match(GeneratePubMsg.class, this::onGenPub)
                .build();
    }

    private void onConfig(ConfigMsg msg) {
        broker = msg.getActor();
    }

    private void onGenPub(GeneratePubMsg msg) {
        System.out.println("Publisher generating message with value : " + msg.getValue());
        broker.tell(new PublishMsg(msg.getTopic(), msg.getValue()), getSelf());
    }

    public static Props props() {
        return Props.create(PublisherActor.class);
    }
}
