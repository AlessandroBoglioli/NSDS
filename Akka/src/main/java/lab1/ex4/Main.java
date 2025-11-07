package lab1.ex4;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import lab1.ex4.messages.ConfigMsg;
import lab1.ex4.messages.SimpleMsg;
import lab1.ex4.messages.SleepMsg;
import lab1.ex4.messages.WakeupMsg;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");

        // Message sending

        client.tell(new ConfigMsg(server), ActorRef.noSender());

        client.tell(new SimpleMsg(), ActorRef.noSender());
        client.tell(new SimpleMsg(), ActorRef.noSender());
        client.tell(new SleepMsg(), ActorRef.noSender());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.tell(new SimpleMsg(), ActorRef.noSender());
        client.tell(new SimpleMsg(), ActorRef.noSender());
        client.tell(new WakeupMsg(), ActorRef.noSender());

        sys.terminate();
    }
}
