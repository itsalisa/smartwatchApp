package com.example.ttgo_smartwatch_app;

import static com.example.ttgo_smartwatch_app.ForegroundService.EXTRA_MAC_ADDRESS;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ttgo_smartwatch_app.database.DatabaseManager;

public class MainActivity extends AppCompatActivity {

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

}