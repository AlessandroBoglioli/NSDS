package Lab1.Ex3;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.counter.CounterActor;

import java.util.HashMap;
import java.util.Map;

public class ServerActor extends AbstractActor {

    private Map<String, String> record;

    public ServerActor() {
        record = new HashMap<String, String>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder().match(PutMsg.class, this::onPutMsg)
                .match(GetMsg.class, this::onGetMsg)
                .build();
    }

    private void onPutMsg(PutMsg msg) {
        record.put(msg.getName(), msg.getEmail());
    }

    private void onGetMsg(GetMsg msg) {
        if (record.containsKey(msg.getName())) {
            this.getSender().tell(new ReplyMsg(record.get(msg.getName())), getSelf());      // We could also tell directly a String
        } else {
            this.getSender().tell(new ReplyMsg("Not found"), getSelf());
        }
    }

    static Props props() {
        return Props.create(ServerActor.class);
    }

}
