package com.dongholab.ticketbot.api;

import com.dongholab.ticketbot.data.Check;
import com.dongholab.ticketbot.data.SoldoutList;

import retrofit.http.GET;

public interface TicketbotAPI {

    @GET("")
    SoldoutList getSoldoutList();

    @GET("")
    Check setReservate();

}