package lab1.ex3;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

public class Main {

    public static void main(String[] args) {

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");
        final ActorRef server = sys.actorOf(ServerActor.props(), "server");

        client.tell(new ConfigMsg(server), ActorRef.noSender());

        client.tell(new PutMsg("Boglio", "alessandroboglioli@gmail.com"), ActorRef.noSender());
        client.tell(new GetMsg("Boglio"), ActorRef.noSender());

    }

}