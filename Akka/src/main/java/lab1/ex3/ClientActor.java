package lab1.ex3;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class ClientActor extends AbstractActor {

    public ClientActor() { }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(ReplyMsg.class, this::displayEmail).build();
    }

    private void displayEmail(ReplyMsg msg) {
        System.out.println(msg.getEmail());
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }

}
