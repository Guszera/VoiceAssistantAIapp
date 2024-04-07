package com.example.voiceassistantaiapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if we have record audio permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
        setupServiceButtons();
    }

    private void setupServiceButtons() {
        Button startServiceButton = findViewById(R.id.startServiceButton);
        startServiceButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, VoiceRecognitionService.class);
            startService(serviceIntent);
        });

        Button stopServiceButton = findViewById(R.id.stopServiceButton);
        stopServiceButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(MainActivity.this, VoiceRecognitionService.class);
            stopService(serviceIntent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
            } else {
                // Permission was denied or request was cancelled
            }
        }
    }
    private final BroadcastReceiver speechReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("ACTION_RECOGNIZED_TEXT".equals(intent.getAction())) {
                final String recognizedText = intent.getStringExtra("recognizedText");
                runOnUiThread(() -> {
                    TextView recognizedTextView = findViewById(R.id.recognizedTextView);
                    recognizedTextView.setText(recognizedText);
                    handleCommand(intent); // Handle specific commands
                });
            }
        }
    };

    private void handleCommand(Intent intent) {
        if (intent.hasExtra("command")) {
            String command = intent.getStringExtra("command");
            switch (command) {
                case "openSettings":
                    startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    break;
                case "tellTime":
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String currentTime = "The current time is " + sdf.format(new Date());
                    TextView recognizedTextView = findViewById(R.id.recognizedTextView);
                    recognizedTextView.setText(currentTime);
                    break;
                case "findPetrolStation":
                    // Attempt to open Waze and search for the nearest petrol station
                    try {
                        // Use the Waze URI to perform a search. You might need to adjust the query.
                        String uri = "https://waze.com/ul?q=petrol%20station";
                        Intent wazeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        wazeIntent.setPackage("com.waze"); // Optional: Ensure Waze app handles it
                        startActivity(wazeIntent);
                    } catch (ActivityNotFoundException e) {
                        // Waze app not installed, optionally prompt the user to install it
                        Toast.makeText(MainActivity.this, "Waze is not installed", Toast.LENGTH_SHORT).show();
                    }
                    break;
                // Add more cases for other commands
            }
        }
    }
    @SuppressLint({"InlinedApi", "UnspecifiedRegisterReceiverFlag"})
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("ACTION_RECOGNIZED_TEXT");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Corrected call for Android 12 and above
            registerReceiver(speechReceiver, filter, null, null, Context.RECEIVER_NOT_EXPORTED);
        } else {
            // Fallback for earlier Android versions
            registerReceiver(speechReceiver, filter);
        }
        Log.d("MainActivity", "Receiver registered");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(speechReceiver);
        Log.d("MainActivity", "Receiver unregistered");
    }
}
