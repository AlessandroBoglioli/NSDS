package lab.ex3;

public class GetMsg extends Msg{

    private String name;

    public GetMsg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
