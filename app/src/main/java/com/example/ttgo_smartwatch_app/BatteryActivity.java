package com.example.ttgo_smartwatch_app;

import android.os.Bundle;
import android.view.View;

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
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.ttgo_smartwatch_app.database.DatabaseManager;
import com.example.ttgo_smartwatch_app.database.entity.Movement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class BatteryActivity extends AppCompatActivity {

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        databaseManager = new DatabaseManager(getApplicationContext());

        setupViews();
    }

    private void setupViews() {
        setTitle("Battery");
        setupThirdChart();
    }

    private void setupThirdChart() {
        runOnBackground(() -> {
            showLoading(true);
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