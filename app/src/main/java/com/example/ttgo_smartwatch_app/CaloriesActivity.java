package com.example.ttgo_smartwatch_app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.example.ttgo_smartwatch_app.database.DatabaseManager;
import com.example.ttgo_smartwatch_app.database.entity.Movement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class CaloriesActivity extends AppCompatActivity {

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calories);
        databaseManager = new DatabaseManager(getApplicationContext());

        setupViews();
    }

    private void setupViews() {
        setTitle("Activity");
        setupFirstChart();
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

    private void runOnBackground(Runnable action) {
        new Thread(action).start();
    }

}