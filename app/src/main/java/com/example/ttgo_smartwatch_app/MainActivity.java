package com.example.ttgo_smartwatch_app;

import static com.example.ttgo_smartwatch_app.ForegroundService.EXTRA_MAC_ADDRESS;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.example.ttgo_smartwatch_app.database.DatabaseManager;
import com.example.ttgo_smartwatch_app.database.entity.Date;
import com.example.ttgo_smartwatch_app.database.entity.Location;
import com.example.ttgo_smartwatch_app.database.entity.Movement;
import com.example.ttgo_smartwatch_app.database.entity.Time;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final static long ONE_HOUR =  60 * 60 * 1000; // millis

    TextView mBuffer;
    AnyChartView chartView;

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBuffer = findViewById(R.id.buffer);
        chartView = findViewById(R.id.any_chart_view);
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
        setupFirstChart();
        setupSecondChart();
        runOnBackground(() -> {
            List<Movement> movements = databaseManager.dao.getAllMovements();
            List<Date> dates = databaseManager.dao.getAllDates();
            List<Time> times = databaseManager.dao.getAllTimes();
            List<Location> locations = databaseManager.dao.getAllLocations();
            if (movements.isEmpty() || dates.isEmpty() || times.isEmpty() || locations.isEmpty()) {
                return;
            }

            final String text = "battery = " + movements.get(0).battery + ", "
                    + "temperature = " + movements.get(0).temperature + ", "
                    + "Is Charging = " + movements.get(0).isCharging + ", "
                    + "Accelerometer X = " + movements.get(0).accelerometerX + ", "
                    + "Accelerometer Y = " + movements.get(0).accelerometerY + ", "
                    + "Accelerometer Z = " + movements.get(0).accelerometerZ + ", "
                    + "Step Counter = " + movements.get(0).StepCounter + ", "
                    + "Year = " + dates.get(0).year + ", "
                    + "Month = " + dates.get(0).month + ", "
                    + "Day = " + dates.get(0).day + ", "
                    + "Hour = " + times.get(0).hour + ", "
                    + "Minutes = " + times.get(0).minutes + ", "
                    + "Seconds = " + times.get(0).seconds + ", "
                    + "Latitude = " + locations.get(0).lattitude + ", "
                    + "Longitude = " + locations.get(0).longitude;
            runOnUiThread(() -> {
                // some UI code
                mBuffer.setText(text);
            });
        });
    }

    private void setupFirstChart() {

        runOnBackground(() -> {
            // Prepare data from database

            // Key hour, value step count
            HashMap<Integer, Integer> preparedData = new HashMap<>();
            long startDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
            List<Movement> movements = databaseManager.dao.getLastMovements(startDate);

            int hours = 1;
            for (Movement movement : movements) {
                long intervalStart = startDate + (hours - 1) * ONE_HOUR; // 20.00
                long intervalEnd = startDate + (hours) * ONE_HOUR; // 21.00
                if (movement.timeStamp > intervalStart && movement.timeStamp < intervalEnd) {
                    preparedData.put(hours, movement.StepCounter);
                } else if (movement.timeStamp > intervalEnd) {
                    hours++;
                }
            }

            // Put data to chart
            List<DataEntry> data = new ArrayList<>();
            for (Map.Entry<Integer, Integer> entry : preparedData.entrySet()) {
                data.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
            }

            // Prepare chart stuff
            Cartesian cartesian = AnyChart.column();
            Column column = cartesian.column(data);

            column.tooltip()
                    .titleFormat("{%X}")
                    .position(Position.CENTER_BOTTOM)
                    .anchor(Anchor.CENTER_BOTTOM)
                    .offsetX(0d)
                    .offsetY(5d)
                    .format("${%Value}{groupsSeparator: }");

            cartesian.animation(true);
            cartesian.title("Movement Chart");

            cartesian.yScale().minimum(0d);

            cartesian.yAxis(0).labels().format("${%Value}{groupsSeparator: }");

            cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
            cartesian.interactivity().hoverMode(HoverMode.BY_X);

            cartesian.xAxis(0).title("Data Type");
            cartesian.yAxis(0).title("Value");

            runOnUiThread(() -> {
                chartView.setChart(cartesian);
            });
        });
    }

    public void setupSecondChart() {
        runOnBackground(() -> {
        List<Movement> movements = databaseManager.dao.getAllMovements();

        ArrayList<DataEntry> stepCountData = new ArrayList<>();
        ArrayList<DataEntry> caloriesBurnedData = new ArrayList<>();

        for (Movement movement : movements) {
            float steps = movement.StepCounter;
            stepCountData.add(new ValueDataEntry(String.valueOf(movement.uid), steps));

            float calories = steps / 20; // 100 calories burned per 2000 steps
            caloriesBurnedData.add(new ValueDataEntry(String.valueOf(movement.uid), calories));
        }
        Set set1 = Set.instantiate();
        set1.data(stepCountData);
        Mapping stepCountMapping = set1.mapAs("{ x: 'x', value: 'value' }");

        Set set2 = Set.instantiate();
        set2.data(caloriesBurnedData);
        Mapping caloriesBurnedMapping = set2.mapAs("{ x: 'x', value: 'value2' }");

        Cartesian cartesian = AnyChart.cartesian();

        cartesian.title("Step Count and Calories Burned");

        Line stepCountLine = cartesian.line(stepCountMapping);
        stepCountLine.name("Step Count");
        stepCountLine.color("#1976d2");

        Column caloriesBurnedColumn = cartesian.column(caloriesBurnedMapping);
        caloriesBurnedColumn.name("Calories Burned");
        caloriesBurnedColumn.color("#ef5350");

        cartesian.legend().enabled(true);

        cartesian.yAxis(0).title("Step Count");
        cartesian.yAxis(1).title("Calories Burned");

        AnyChartView anyChartView = findViewById(R.id.any_chart_view);
        anyChatView.setChart(cartesian);
            runOnUiThread(() -> {
                chartView.setChart(cartesian);}
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

}