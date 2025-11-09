package lab1.ex2022.messsages;

import akka.actor.ActorRef;

public class GeneratePubMsg {

    private String topic;
    private String value;

    public GeneratePubMsg (String topic, String value) {
        this.topic = topic;
        this.value = value;
    }

    public String getTopic() {
        return topic;
    }

    public String getValue() {
        return value;
    }

}
