package com.example.voiceassistantaiapp;
import android.widget.Toast;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import android.util.Log;



import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;

    private TextView listeningStateTextView;
    private TextView notListeningStateTextView;
    private TextView wakeWordDetectedTextView;
    private TextView recognizedSpeechTextView;

    private Button startServiceButton;
    private Button stopServiceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize TextViews
        listeningStateTextView = findViewById(R.id.listeningStateTextView);
        notListeningStateTextView = findViewById(R.id.notListeningStateTextView);
        wakeWordDetectedTextView = findViewById(R.id.wakeWordDetectedTextView);
        recognizedSpeechTextView = findViewById(R.id.recognizedSpeechTextView);
        // Initialize Buttons
        startServiceButton = findViewById(R.id.startServiceButton);
        stopServiceButton = findViewById(R.id.stopServiceButton);

        // Set onClickListener for startServiceButton
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    // Start the service when startServiceButton is clicked
                    // Hide previous state TextViews
                    listeningStateTextView.setVisibility(View.GONE);
                    notListeningStateTextView.setVisibility(View.GONE);
                    wakeWordDetectedTextView.setVisibility(View.GONE);

                    startListening();
                } else {
                    requestPermission();
                }
            }
        });

        // Set onClickListener for stopServiceButton
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide previous state TextViews
                listeningStateTextView.setVisibility(View.GONE);
                notListeningStateTextView.setVisibility(View.GONE);
                wakeWordDetectedTextView.setVisibility(View.GONE);
                // Stop the service when stopServiceButton is clicked
                stopListening();
            }
        });

        // Register BroadcastReceiver to receive UI update messages from VoiceRecognitionService
        registerReceiver(uiUpdateReceiver, new IntentFilter("UPDATE_RECOGNIZED_SPEECH"), Context.RECEIVER_NOT_EXPORTED);
    }

    private boolean checkPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSION_REQUEST_RECORD_AUDIO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start listening
                startListening();
            } else {
                // Permission denied, show a message or handle accordingly
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister BroadcastReceiver
        unregisterReceiver(uiUpdateReceiver);
    }

    private BroadcastReceiver uiUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "Received broadcast: " + intent.getAction());
            switch (intent.getAction()) {
                case "UPDATE_RECOGNIZED_SPEECH":
                    String recognizedSpeech = intent.getStringExtra("recognizedSpeech");
                    if (recognizedSpeech != null) {
                        recognizedSpeechTextView.setVisibility(View.VISIBLE);
                        recognizedSpeechTextView.setText(recognizedSpeech);
                    }
                    break;
                case "UPDATE_UI_LISTENING_STATE":
                    // Assuming you send such intents from the service
                    boolean isListening = intent.getBooleanExtra("isListening", false);
                    listeningStateTextView.setVisibility(isListening ? View.VISIBLE : View.GONE);
                    notListeningStateTextView.setVisibility(isListening ? View.GONE : View.VISIBLE);
                    break;
                case "UPDATE_UI_WAKE_WORD_DETECTED":
                    // Assuming you send such intents from the service
                    boolean isWakeWordDetected = intent.getBooleanExtra("isWakeWordDetected", false);
                    wakeWordDetectedTextView.setVisibility(isWakeWordDetected ? View.VISIBLE : View.GONE);
                    break;
            }
        }
    };
    private void startListening() {
        // Start the VoiceRecognitionService to begin listening for the wake word detection
        Intent serviceIntent = new Intent(this, VoiceRecognitionService.class);
        startService(serviceIntent);
        listeningStateTextView.setVisibility(View.VISIBLE);
    }

    private void stopListening() {
        // Stop the VoiceRecognitionService to stop listening for the wake word detection
        Intent serviceIntent = new Intent(this, VoiceRecognitionService.class);
        stopService(serviceIntent);
        notListeningStateTextView.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_RECOGNIZED_SPEECH");
        filter.addAction("UPDATE_UI_LISTENING_STATE");
        filter.addAction("UPDATE_UI_WAKE_WORD_DETECTED");
        registerReceiver(uiUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(uiUpdateReceiver);
    }
}