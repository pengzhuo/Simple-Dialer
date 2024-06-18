package com.simplemobiletools.dialer.helpers;

public class MessageEvent {
    private String message;
    private int number;

    public MessageEvent(int number, String message){
        this.message = message;
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
