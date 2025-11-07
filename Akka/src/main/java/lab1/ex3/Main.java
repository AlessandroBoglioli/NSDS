package lab1.ex3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

    public static void main(String[] args) {

        // TODO: Add ask pattern

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");

        // TODO: change the message sequence, send a client a message to perform a sending to the server by himself

        server.tell(new PutMsg("Boglio", "alessandroboglioli@gmail.com"), client);
        server.tell(new GetMsg("Boglio"), client);
    }
}
