package Lab1.Ex4;

import akka.actor.ActorRef;

public class CommandMsg {

    private ActorRef destination;
    private int command;

    public CommandMsg(ActorRef destination, int command) {
        this.destination = destination;
        this.command = command;
    }

    public ActorRef getDestination() {
        return destination;
    }

    public int getCommand() {
        return command;
    }

}
