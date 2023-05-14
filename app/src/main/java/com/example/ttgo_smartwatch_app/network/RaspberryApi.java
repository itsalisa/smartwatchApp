package com.example.ttgo_smartwatch_app.network;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RaspberryApi {

    @POST("/movements-script.php")
    Call<Object> postMovements(@Body JsonArray body);

    @POST("/locations-script.php")
    Call<Object> postLocations(@Body JsonArray body);

}
