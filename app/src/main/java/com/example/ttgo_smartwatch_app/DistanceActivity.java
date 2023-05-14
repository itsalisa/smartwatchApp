package com.example.ttgo_smartwatch_app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Waterfall;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.ttgo_smartwatch_app.database.DatabaseManager;
import com.example.ttgo_smartwatch_app.database.entity.Location;
import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.example.ttgo_smartwatch_app.network.RaspberryRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class DistanceActivity extends AppCompatActivity {

    DatabaseManager databaseManager;
    RaspberryRepository repository = new RaspberryRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distance);
        databaseManager = new DatabaseManager(getApplicationContext());

        setupViews();
    }

    private void setupViews() {
        setTitle("Distance");
        setupSecondChart();
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double earthRadius = 6371; // km

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance;
    }

    private void setupSecondChart() {
        runOnBackground(() -> {
            showLoading(true);
            // Get data from the locations and movements database
            List<Location> locations = databaseManager.dao.getAllLocations();
            List<Movement> movements = databaseManager.dao.getAllMovements();

            repository.sendMovements(movements);
            repository.sendLocations(locations);

            // Creating a harshmap to store locations by hour
            HashMap<Integer, List<Location>> locationsByHour = new HashMap<>();
            for (Location location : locations) {
                // Getting hour from location timestamp
                long timestamp = location.timeStamp;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                // Add the location to the list for this hour
                if (locationsByHour.containsKey(hour)) {
                    locationsByHour.get(hour).add(location);
                } else {
                    List<Location> hourLocations = new ArrayList<>();
                    hourLocations.add(location);
                    locationsByHour.put(hour, hourLocations);
                }
            }

            // Creating an array list of data entries for the chart
            ArrayList<DataEntry> data = new ArrayList<>();
            // For each hour of the day get the locations and movements for this hour
            for (int hour = 0; hour < 24; hour++) {
                List<Location> hourLocations = locationsByHour.containsKey(hour) ? locationsByHour.get(hour) : new ArrayList<>();
                int hourSteps = 0;
                double hourDistance = 0;
                for (int i = 0; i < hourLocations.size(); i++) {
                    // Calculating the distance between each pair of locations
                    if (i > 0) {
                        Location prevLocation = hourLocations.get(i - 1);
                        Location currLocation = hourLocations.get(i);
                        double prevLat = prevLocation.lattitude;
                        double prevLong = prevLocation.longitude;
                        double currLat = currLocation.lattitude;
                        double currLong = currLocation.longitude;
                        double distance = calculateDistance (prevLat, prevLong, currLat, currLong);
                        hourDistance += distance;
                    }
                }
                // Getting the number of steps for this hour
                for (Movement movement : movements) {
                    long timestamp = movement.timeStamp;
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(timestamp);
                    if (calendar.get(Calendar.HOUR_OF_DAY) == hour) {
                        hourSteps += movement.StepCounter;
                    }
                }
                // Adding the data entries for this hour to the arraylist
                data.add(new ValueDataEntry(String.valueOf(hour), hourDistance));
                data.add(new ValueDataEntry("Starting point", 0));
                data.add(new ValueDataEntry("Distance", hourDistance));
                data.add(new ValueDataEntry("Steps", hourSteps));
            }

            // Creating and customising waterfall chart
            Waterfall distanceChart = AnyChart.waterfall();
            distanceChart.animation(true);
            distanceChart.padding(10d, 20d, 5d, 20d);
            distanceChart.crosshair().enabled(true);
            distanceChart.crosshair()
                    .yLabel(true)
                    .yStroke((Stroke) null, null, null, (String) null, (String) null);
            distanceChart.tooltip().positionMode(TooltipPositionMode.POINT);
            distanceChart.title("Distance and Steps");

            // Formatting chart labels
            distanceChart.labels().format(
                    "function() {\n" +
                            "      if (this['isTotal']) {\n" +
                            "        return anychart.format.number(this.absolute, {\n" +
                            "          scale: true\n" +
                            "        })\n" +
                            "      }\n" +
                            "\n" +
                            "      return anychart.format.number(this.value, {\n" +
                            "        scale: true\n" +
                            "      })\n" +
                            "    }");

            distanceChart.legend().enabled(true);
            distanceChart.legend().fontSize(13d);
            distanceChart.legend().padding(10d, 25d, 10d, 25d);

            distanceChart.yAxis(0).title("KM");
            distanceChart.xAxis(0).title("Steps");

            AnyChartView distanceChartView = findViewById(R.id.second_chart_view);

            runOnUiThread(() -> {
                distanceChartView.setChart(distanceChart);
                showLoading(false);
            });
        });
    }

    private void runOnBackground(Runnable action) {
        new Thread(action).start();
    }

    private void showLoading(boolean show) {
        runOnUiThread(() -> {
            View loading = findViewById(R.id.loading);
            if (show) {
                loading.setVisibility(View.VISIBLE);
            } else {
                loading.setVisibility(View.GONE);
            }

        });
    }

}

