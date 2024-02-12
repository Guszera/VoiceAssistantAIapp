package com.example.voiceassistantaiapp;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import android.content.Intent;
import android.widget.TextView;
import android.util.Log;



public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private SpeechRecognizer speechRecognizer;
    private static final String TAG = "startListening";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
        } else {
            startListening();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening();
            } else {
                Toast.makeText(this, "Permission denied to record audio", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startListening() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the recognizer is ready to start listening,
                // Can perform any specific action when the recogniser is ready
                Toast.makeText(MainActivity.this, "Speech recognition is ready. You can start speaking.", Toast.LENGTH_SHORT).show();
            }


            @Override
            public void onBeginningOfSpeech() {
                // Called when the user starts to speak
                Toast.makeText(MainActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Called when the RMS value of the audio being processed changes
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Called after the user stops speaking, and the recogniser is processing the audio
                Toast.makeText(MainActivity.this, "Processing...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEndOfSpeech() {
                // Called when the user finishes speaking
                Toast.makeText(MainActivity.this, "Stopping listening.", Toast.LENGTH_SHORT).show();
                startListening();
            }

            @Override
            public void onError(int error) {
                // Called when an error occurs during recognition
                String errorMessage;
                switch (error) {
                    case SpeechRecognizer.ERROR_AUDIO:
                        errorMessage = "Audio recording error";
                        break;
                    case SpeechRecognizer.ERROR_CLIENT:
                        errorMessage = "Client side error";
                        break;
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        errorMessage = "Insufficient permissions";
                        break;
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        errorMessage = "No recognition result matched";
                        break;
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        errorMessage = "RecognitionService busy";
                        break;
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        errorMessage = "No speech input";
                        break;
                    default:
                        errorMessage = "Unknown error";
                        break;
                }
                Log.e(TAG, "Recognition error: " + errorMessage);
                // Handle error correctly displaying an error message
            }

            @Override
            public void onResults(Bundle results) {
                // Called when recognition results are available,
                // handling the recognition results and taking specific actions
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String recognizedText = matches.get(0); // Get the first recognition result
                    TextView textView = findViewById(R.id.recognizedText);
                    textView.setText("Recognized Text: " + recognizedText); // Update TextView with recognised text
                    for (String result : matches) {
                        if (result.equalsIgnoreCase("test")) {
                            // Wake up word detected, perform action display message.
                            Toast.makeText(MainActivity.this, "Wake up word detected!", Toast.LENGTH_SHORT).show();
                            break; // Exit loop after detecting the wake up word
                        }
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called when partial recognition results are available
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when events related to the recognition are available
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
    }
}
