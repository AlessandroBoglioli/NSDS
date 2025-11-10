package com.lab.evaluation25;

public class ReplyMsg extends ResponseMsg{

    private String email;

    public ReplyMsg(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
