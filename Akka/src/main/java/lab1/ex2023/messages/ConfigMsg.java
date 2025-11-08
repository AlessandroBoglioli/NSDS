package lab1.ex2023.messages;

import akka.actor.ActorRef;

public class ConfigMsg {

    private ActorRef actor;

    public ConfigMsg(ActorRef actor) {
        this.actor = actor;
    }

    public ActorRef getActor() {
        return actor;
    }

}
