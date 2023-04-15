package com.example.ttgo_smartwatch_app.network;

import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RaspberryRepository {

    RaspberryApi api = RetrofitClientInstance.retrofit().create(RaspberryApi.class);

    public void sendMovements(List<Movement> movements) {
        runOnBackground( () -> {
            JsonArray array = new JsonArray();
            for (Movement m : movements) {
                JsonObject object = new JsonObject();
                object.addProperty("battery", m.battery);
                //TODO m parse to JSON object
                array.add(object);
            }
            Call<Object> call = api.postMovements(array);
            call.enqueue(new Callback<Object>() {
                @Override public void onResponse(Call<Object> call, Response<Object> response) {}
                @Override public void onFailure(Call<Object> call, Throwable t) {}
            });
        });
    }


    private void runOnBackground(Runnable action) {
        try {
            new Thread(action).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
