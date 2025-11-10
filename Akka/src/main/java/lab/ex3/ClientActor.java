package lab.ex3;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ClientActor extends AbstractActor {

    private ActorRef server;

    public ClientActor() { }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(GetMsg.class, this::redirect)
                .match(PutMsg.class, this::redirect)
                .match(ReplyMsg.class, this::displayEmail)
                .match(ConfigMsg.class, this::configServer)
                .build();
    }

    private void redirect(Msg msg) {
        server.tell(msg, getSelf());
    }

    private void configServer(ConfigMsg msg) {
        server = msg.getServer();
    }

    private void displayEmail(ReplyMsg msg) {
        System.out.println(msg.getEmail());
    }

    static Props props() {
        return Props.create(ClientActor.class);
    }

}
