package lab1.ex5;

public class GetMsg extends Msg {

    private String name;

    public GetMsg(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
