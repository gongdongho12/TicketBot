package com.dongholab.ticketbot.data;

public class Check {
    boolean success;
    String message;
    int delay_seconds;

    public boolean getSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public int getDelay_seconds() {
        return this.delay_seconds;
    }
}
