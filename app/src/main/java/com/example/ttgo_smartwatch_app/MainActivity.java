package com.example.ttgo_smartwatch_app;

import static com.example.ttgo_smartwatch_app.ForegroundService.EXTRA_MAC_ADDRESS;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ttgo_smartwatch_app.database.DatabaseManager;
import com.example.ttgo_smartwatch_app.database.entity.Movement;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    TextView mBuffer;

    DatabaseManager databaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBuffer = findViewById(R.id.buffer);
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
        runOnBackground(() -> {
            List<Movement> movements = databaseManager.dao.getAllMovements();
            final String text = movements.get(0).battery + "";
            runOnUiThread(() -> {
                // some UI code
                mBuffer.setText(text);
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

}