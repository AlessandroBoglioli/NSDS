package Lab1.Ex3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.counter.CounterActor;

public class Main {

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");

        server.tell(new PutMsg("Boglio", "alessandroboglioli@gmail.com"), client);
        server.tell(new GetMsg("Boglio"), client);
    }
}
