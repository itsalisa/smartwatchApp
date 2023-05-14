package com.example.ttgo_smartwatch_app.network;

import com.example.ttgo_smartwatch_app.database.entity.Location;
import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

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
                object.addProperty("accelerometerX", m.accelerometerX);
                object.addProperty("accelerometerY", m.accelerometerY);
                object.addProperty("accelerometerZ", m.accelerometerZ);
                object.addProperty("stepCounter", m.StepCounter);
                object.addProperty("timestamp", m.timeStamp);
                array.add(object);
            }
            Call<Object> call = api.postMovements(array);
            call.enqueue(new Callback<Object>() {
                @Override public void onResponse(Call<Object> call, Response<Object> response) {}
                @Override public void onFailure(Call<Object> call, Throwable t) {}
            });
        });
    }

    public void sendLocations(List<Location> locations) {
        runOnBackground( () -> {
            JsonArray array = new JsonArray();
            for (Location l : locations) {
                JsonObject object = new JsonObject();
                object.addProperty("latitude", l.lattitude);
                object.addProperty("longitude", l.longitude);
                object.addProperty("timestamp", l.timeStamp);
                array.add(object);
            }
            Call<Object> call = api.postLocations(array);
            call.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {}
                @Override
                public void onFailure(Call<Object> call, Throwable t) {}
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
