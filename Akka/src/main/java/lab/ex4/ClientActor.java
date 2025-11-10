package lab.ex4;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import lab.ex4.messages.*;

public class ClientActor extends AbstractActor {

    private ActorRef server;

    public ClientActor() { }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReplyMsg.class, this::onReplyMsg)
                .match(SimpleMsg.class, this::redirectMsg)
                .match(SleepMsg.class, this::redirectMsg)
                .match(WakeupMsg.class, this::redirectMsg)
                .match(ConfigMsg.class, this::onConfigMsg)
                .build();
    }

    private void onConfigMsg(ConfigMsg msg) {
        server = msg.getServerRef();
    }

    void onReplyMsg(ReplyMsg msg) {
        System.out.println("message recived");
    }

    private void redirectMsg(Msg msg) {
        server.tell(msg, self());
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }
}
