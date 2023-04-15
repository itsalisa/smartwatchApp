package com.example.ttgo_smartwatch_app.network;

import com.google.gson.JsonArray;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RaspberryApi {

    @POST("/api/movements")
    Call<Object> postMovements(@Body JsonArray body);

}
