package lab1.ex2022;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lab1.ex2022.messsages.ConfigMsg;
import lab1.ex2022.messsages.GenerateSubMsg;
import lab1.ex2022.messsages.NotifyMsg;
import lab1.ex2022.messsages.SubscribeMsg;

public class SubscriberActor extends AbstractActor {

    private ActorRef broker;

    public SubscriberActor() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ConfigMsg.class, this::onConfig)
                .match(GenerateSubMsg.class, this::onGenSub)
                .match(NotifyMsg.class, this::onNotify)
                .build();
    }

    private void onConfig(ConfigMsg msg) {
        broker = msg.getActor();
    }

    private void onGenSub(GenerateSubMsg msg) {
        broker.tell(new SubscribeMsg(msg.getTopic(), getSelf()), getSelf());
    }

    private void onNotify(NotifyMsg msg) {
        System.out.println("Subscriber recived a message : " + msg.getValue());
    }

    public static Props props() {
        return Props.create(SubscriberActor.class);
    }
}
