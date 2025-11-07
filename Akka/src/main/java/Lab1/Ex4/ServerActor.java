package Lab1.Ex4;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class ServerActor extends AbstractActorWithStash {

    public ServerActor() { }

    @Override
    public Receive createReceive() {
        return awake();
    }

    public Receive awake() {
        return receiveBuilder().match(SleepMsg.class, this::sleep)
                .match(SimpleMsg.class, this::sendMessages)
                .build();
    }

    public Receive sleeping() {
        return receiveBuilder().match(WakeupMsg.class, this::wakeup)
                .match(SimpleMsg.class, this::stashMessages)
                .build();
    }

    void sleep(SleepMsg msg) {
        getContext().become(sleeping());
    }

    void wakeup(WakeupMsg msg) {
        getContext().become(awake());
        unstashAll();
    }

    void stashMessages(SimpleMsg msg) {
        stash();
    }

    void sendMessages(SimpleMsg msg) {
        this.getSender().tell(new ReplyMsg(), this.getSelf());
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }
}