package com.example.ttgo_smartwatch_app;

import static com.example.ttgo_smartwatch_app.ForegroundService.EXTRA_MAC_ADDRESS;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Waterfall;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.ttgo_smartwatch_app.database.DatabaseManager;
import com.example.ttgo_smartwatch_app.database.entity.Date;
import com.example.ttgo_smartwatch_app.database.entity.Location;
import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.example.ttgo_smartwatch_app.database.entity.Time;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //mBuffer = findViewById(R.id.buffer);
        //chartView = findViewById(R.id.any_chart_view);
        databaseManager = new DatabaseManager(getApplicationContext());

        setupViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.your_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.connect_to_bt:
                showConnectionDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViews() {
       // setupFirstChart();
       // setupSecondChart();
       // setupThirdChart();

        findViewById(R.id.runDistanceButton).setOnClickListener(v -> {
            startActivity(new Intent(this, DistanceActivity.class));
        });

        findViewById(R.id.runCaloriesButton).setOnClickListener(v -> {
            startActivity(new Intent(this, CaloriesActivity.class));
        });

        findViewById(R.id.runBatteryButton).setOnClickListener(v -> {
            startActivity(new Intent(this, BatteryActivity.class));
        });

    }

    public void setupFirstChart() {
        // Run queries on background to avoid blocking the main UI thread
        runOnBackground(() -> {
        List<Movement> movements = databaseManager.dao.getAllMovements(); // Retrieve from database

            // Group movements by hour
            HashMap<Integer, Float> stepsByHour = new HashMap<>();
            for (Movement movement : movements) {
                long timestamp = movement.timeStamp;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                float steps = movement.StepCounter;
                if (stepsByHour.containsKey(hour)) {
                    steps = stepsByHour.get(hour);
                }
                stepsByHour.put(hour, steps);
            }
        // Initialise lists for the data
        ArrayList<DataEntry> stepCountData = new ArrayList<>();
        ArrayList<DataEntry> caloriesBurnedData = new ArrayList<>();

        // Go through stepByHour and populate data lists
        for (int hour = 0; hour < 24; hour ++) {
            float steps = stepsByHour.containsKey(hour) ? stepsByHour.get(hour) : 0;
            // Add new data entry to list
            stepCountData.add(new ValueDataEntry(String.valueOf(hour), steps));

            float calories = steps / 20; // Calculate calories burned - 100 calories burned per 2000 steps
            caloriesBurnedData.add(new ValueDataEntry(String.valueOf(hour), calories));
        }
        // Initialising set of data for the chart
        Set set1 = Set.instantiate();
        set1.data(stepCountData);
        // Map the data to the chart format
        Mapping stepCountMapping = set1.mapAs("{ x: 'x', value: 'value' }");

        Set set2 = Set.instantiate();
        set2.data(caloriesBurnedData);
        Mapping caloriesBurnedMapping = set2.mapAs("{ x: 'x', value: 'value2' }");

        // New chart instance
        Cartesian activityChart = AnyChart.cartesian();

            activityChart.title("Step Count and Calories Burned");

        // Line series for the step count data
        Line stepCountLine = activityChart.line(stepCountMapping);
        stepCountLine.name("Step Count");
        stepCountLine.color("#1976d2");

        Column caloriesBurnedColumn = activityChart.column(caloriesBurnedMapping);
        caloriesBurnedColumn.name("Calories Burned");
        caloriesBurnedColumn.color("#ef5350");

        activityChart.legend().enabled(true);

        activityChart.yAxis(0).title("Step Count");
        activityChart.yAxis(1).title("Calories Burned");

        AnyChartView activityChartView = findViewById(R.id.first_chart_view);

            // Update the UI on the main thread with the new chart
            runOnUiThread(() -> {
                activityChartView.setChart(activityChart);
            });
        });
    }

    private void setupSecondChart() {
        runOnBackground(() -> {
            // Get data from the locations and movements database
        List<Location> locations = databaseManager.dao.getAllLocations();
        List<Movement> movements = databaseManager.dao.getAllMovements();

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

        AnyChartView distanceChartView = findViewById(R.id.second_chart_view);

        runOnUiThread(() -> {
            distanceChartView.setChart(distanceChart);
        });
    });
}

    private void setupThirdChart() {
        runOnBackground(() -> {
            List<Movement> movements = databaseManager.dao.getAllMovements();

            // Group by hour
            HashMap<Integer, Integer> batteryByHour = new HashMap<>();
            for (Movement movement : movements) {
                long timestamp = movement.timeStamp;
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(timestamp);
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int battery = movement.battery;
                if (batteryByHour.containsKey(hour)) {
                    battery = batteryByHour.get(hour);
                }
                batteryByHour.put(hour, battery);
            }

            // Initialising lists for the data
            ArrayList<DataEntry> batteryData = new ArrayList<>();

            // Going through the list and populating data list
            for (int hour = 0; hour < 24; hour++) {
                int battery = batteryByHour.containsKey(hour) ? batteryByHour.get(hour) : 0;
                // Adding new data entry list
                batteryData.add(new ValueDataEntry(String.valueOf(hour), battery));
            }
            // Creating chart
            Cartesian batteryChart = AnyChart.cartesian();
            batteryChart.animation(true);
            batteryChart.padding(10d, 20d, 5d, 20d);
            batteryChart.crosshair().enabled(true);
            batteryChart.crosshair()
                    .yLabel(true)
                    .yStroke((Stroke) null, null, null, (String) null, (String) null);
            batteryChart.tooltip().positionMode(TooltipPositionMode.POINT);
            batteryChart.title("Battery percentage over time");

            Set batteryDataSet = Set.instantiate();
            batteryDataSet.data(batteryData);
            Mapping batteryMapping = batteryDataSet.mapAs("{ x: 'x', value: 'value' }");

            Line batteryLine = batteryChart.line(batteryMapping);
            batteryLine.name("Battery percentage");

            batteryChart.legend().enabled(true);
            batteryChart.legend().fontSize(13d);
            batteryChart.legend().padding(0d, 0d, 10d, 0d);

            batteryChart.yAxis(0).title("%");
            batteryChart.xAxis(0).title("Hour");

        // Display the chart
        AnyChartView batteryChartView = findViewById(R.id.third_chart_view);
            runOnUiThread(() -> {
                batteryChartView.setChart(batteryChart);
            });
        });

    }

    private void showConnectionDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Connect to Watch?")
                .setMessage("Connect to TWatch?")
                .setPositiveButton("Connect", (dialog, which) -> {
                    runService();
                })
                .show();
    }

    private void runService() {
        Intent intent = new Intent(this, ForegroundService.class);
        intent.putExtra(EXTRA_MAC_ADDRESS, "08:3A:F2:69:B5:3E");
        startService(intent);
    }


    private void runOnBackground(Runnable action) {
        new Thread(action).start();
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

}