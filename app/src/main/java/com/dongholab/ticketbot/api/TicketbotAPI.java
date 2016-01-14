package com.dongholab.ticketbot.api;

import com.dongholab.ticketbot.data.Soldout;
import java.util.List;
import java.util.Map;

import retrofit.http.GET;
import retrofit.http.QueryMap;

public interface TicketbotAPI {

    @GET("/KMU/test1.php")
    List<Soldout> getSoldoutList(@QueryMap Map<String, String> query);
}