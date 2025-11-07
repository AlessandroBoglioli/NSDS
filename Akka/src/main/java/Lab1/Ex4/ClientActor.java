package Lab1.Ex4;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ClientActor extends AbstractActor {
    private static final int SIMPLE_MSG = 1;
    private static final int SLEEP_MSG = 2;
    private static final int WAKEUP_MSG = 3;

    public ClientActor() { }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReplyMsg.class, this::onReplyMsg)
                .match(CommandMsg.class, this::onCommandMsg)
                .build();
    }

    void onReplyMsg(ReplyMsg msg) {
        System.out.println("message recived");
    }

    private void onCommandMsg(CommandMsg msg) {
        switch (msg.getCommand()){
            case SIMPLE_MSG : msg.getDestination().tell(new SimpleMsg(), getSelf()); break;
            case SLEEP_MSG : msg.getDestination().tell(new SleepMsg(), getSelf()); break;
            case WAKEUP_MSG : msg.getDestination().tell(new WakeupMsg(), getSelf()); break;
        }

    }

    static int simleMsg() {
        return SIMPLE_MSG;
    }

    static int sleepMsg() {
        return SLEEP_MSG;
    }
    static int wakeUpMsg() {
        return WAKEUP_MSG;
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }
}
