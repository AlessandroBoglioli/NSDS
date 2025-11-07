package Lab1.Ex4;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");

        client.tell(new CommandMsg(server, ClientActor.simleMsg()), ActorRef.noSender());
        client.tell(new CommandMsg(server, ClientActor.simleMsg()), ActorRef.noSender());
        client.tell(new CommandMsg(server, ClientActor.sleepMsg()), ActorRef.noSender());
        client.tell(new CommandMsg(server, ClientActor.simleMsg()), ActorRef.noSender());
        client.tell(new CommandMsg(server, ClientActor.simleMsg()), ActorRef.noSender());

        try{
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.tell(new CommandMsg(server, ClientActor.wakeUpMsg()), ActorRef.noSender());

        // Wait for all messages to be sent and received
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        sys.terminate();
    }
}
