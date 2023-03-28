package com.example.ttgo_smartwatch_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ForegroundService extends Service {

    final static String CHANNEL_ID = "123456";
    final static String EXTRA_MAC_ADDRESS = "EXTRA_MAC_ADDRESS";

    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mBTAdapter;
    private BluetoothSocket mBTSocket = null;

    public final static int MESSAGE_READ = 2;
    private final static int CONNECTING_STATUS = 3;

    private ConnectedThread mConnectedThread;

    private Handler mHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Watch Service")
                .setContentText("Watch Tracking Service Enabled")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        tryToConnect();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
    }

    private void tryToConnect() {

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_READ) {
                    String readMessage = null;
                    readMessage = new String((byte[]) msg.obj, StandardCharsets.UTF_8);
                    parseMessage(readMessage);
                }
            }
        };

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        new Thread(() -> {
            boolean fail = false;

            BluetoothDevice device = mBTAdapter.getRemoteDevice("08:3A:F2:69:B5:3E");

            try {
                mBTSocket = createBluetoothSocket(device);
            } catch (IOException e) {
                fail = true;
                Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_SHORT).show();
            }
            // Establish the Bluetooth socket connection.
            try {
                mBTSocket.connect();
            } catch (IOException e) {
                try {
                    fail = true;
                    mBTSocket.close();
                    mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                            .sendToTarget();
                } catch (IOException e2) {
                    //insert code to deal with this
                    Toast.makeText(getBaseContext(), "ERROR", Toast.LENGTH_SHORT).show();
                }
            }
            if(!fail) {
                mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                mConnectedThread.start();

                mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, "my watch")
                        .sendToTarget();
            }
        }).start();
    }

    private void parseMessage(String readMessage) {
        String[] messages = readMessage.split("\r\n");
        for (String message : messages) {
            try {

                JSONObject obj = new JSONObject(message);

                Log.d("My App", obj.toString());

            } catch (Throwable t) {
                String shortMessage = message.length() > 10
                        ? message.substring(10)
                        : message;
               // Log.e("My App", "Could not parse malformed JSON: \"" + shortMessage + "\"");
            }
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e("TAG", "Could not create Insecure RFComm Connection",e);
        }
        return device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }
}