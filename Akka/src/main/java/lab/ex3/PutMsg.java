package lab.ex3;

public class PutMsg extends Msg{

    private String email;
    private String name;

    public PutMsg(String name, String email) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

}
