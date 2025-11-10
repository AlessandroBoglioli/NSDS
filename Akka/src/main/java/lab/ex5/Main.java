package lab.ex5;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.concurrent.TimeoutException;

import static akka.pattern.Patterns.ask;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Main {

    public static void main(String[] args) {

        scala.concurrent.duration.Duration timeout = scala.concurrent.duration.Duration.create(5, SECONDS);

        final ActorSystem sys = ActorSystem.create("System");
        final ActorRef supervisor = sys.actorOf(ServerSupervisorActor.props(), "supervisor");
        final ActorRef client = sys.actorOf(ClientActor.props(), "client");

        ActorRef server;

        try {

            scala.concurrent.Future<Object> waitingForCounter = ask(supervisor, Props.create(ServerActor.class), 5000);
            server = (ActorRef) waitingForCounter.result(timeout, null);

            client.tell(new ConfigMsg(server), ActorRef.noSender());

            client.tell(new PutMsg("Carlo", "carlo@gmail.com"), ActorRef.noSender());
            client.tell(new PutMsg("Edoardo", "edoardo@gmail.com"), ActorRef.noSender());
            client.tell(new PutMsg("Luca", "luca@gmail.com"), ActorRef.noSender());

            client.tell(new PutMsg("Fail!", "fail@gmail.com"), ActorRef.noSender());

            client.tell(new GetMsg("Carlo"), ActorRef.noSender());
            client.tell(new GetMsg("Edoardo"), ActorRef.noSender());
            client.tell(new GetMsg("Luca"), ActorRef.noSender());

            sys.terminate();

        } catch (TimeoutException | InterruptedException e1) {
            e1.printStackTrace();
        }

    }

}