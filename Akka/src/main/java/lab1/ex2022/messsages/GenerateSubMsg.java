package lab1.ex2022.messsages;

import akka.actor.ActorRef;

public class GenerateSubMsg {

    private String topic;

    public GenerateSubMsg (String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

}
