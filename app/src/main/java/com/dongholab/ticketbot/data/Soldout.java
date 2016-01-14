package com.dongholab.ticketbot.data;

public class Soldout {
    String status;
    int train_number;
    String city_departure;
    String time_departure;
    String city_arrival;
    String time_arrive;
    String train_category;

    public String getStatus() {
        return this.status;
    }

    public int getTrain_number() {
        return this.train_number;
    }

    public String getCity_departure() {
        return this.city_departure;
    }

    public String getTime_departure() {
        return this.time_departure;
    }

    public String getCity_arrival() {
        return this.city_arrival;
    }

    public String getTime_arrive() {
        return this.time_arrive;
    }

    public String getTrain_category() {
        return this.train_category;
    }
}
