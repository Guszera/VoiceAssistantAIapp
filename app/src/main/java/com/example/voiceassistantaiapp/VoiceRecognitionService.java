package com.example.voiceassistantaiapp;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import android.widget.Toast;





public class VoiceRecognitionService extends Service {

    private SpeechRecognizer speechRecognizer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startListening();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopListening();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startListening() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the recognizer is ready to start listening
                updateUIListeningState(true);
                Log.d("VoiceRecognition", "Listening for speech input...");
            }

            @Override
            public void onBeginningOfSpeech() {
                // Called when the user starts to speak
                Log.d("VoiceRecognition", "Beginning of speech");
                Toast.makeText(VoiceRecognitionService.this, "Listening...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // Called when the RMS value of the audio being processed changes
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // Called after the user stops speaking, and the recognizer is processing the audio
                Log.d("VoiceRecognition", "Buffer received");
                Toast.makeText(VoiceRecognitionService.this, "Processing...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEndOfSpeech() {
                // Called when the user finishes speaking
                updateUIListeningState(false);
                Log.d("VoiceRecognition", "End of speech");
            }

            @Override
            public void onError(int error) {
                // Called when an error occurs during recognition
                updateUIListeningState(false);
                Log.e("VoiceRecognition", "Error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    for (String recognizedText : matches) {
                        Log.d("VoiceRecognition", "Recognized speech: " + recognizedText);
                        Intent intent = new Intent("UPDATE_RECOGNIZED_SPEECH");
                        intent.putExtra("recognizedSpeech", recognizedText);
                        sendBroadcast(intent);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String partialText = matches.get(0); // Assuming you want the most confident result
                    Log.d("VoiceRecognition", "Partial results received: " + partialText);
                    // Broadcast partial results
                    Intent intent = new Intent("UPDATE_RECOGNIZED_SPEECH");
                    intent.putExtra("recognizedSpeech", partialText);
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // Called when events related to the recognition are available
                Log.d("VoiceRecognition", "Event: " + eventType);
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        speechRecognizer.startListening(intent);
    }

    private void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
        }
    }

    // Method to update UI for listening state
    private void updateUIListeningState(boolean isListening) {
        Intent intent = new Intent("UPDATE_UI_LISTENING_STATE");
        intent.putExtra("isListening", isListening);
        sendBroadcast(intent);
    }

    // Method to update UI for wake word detection
    private void updateUIWakeWordDetected(boolean isWakeWordDetected) {
        Intent intent = new Intent("UPDATE_UI_WAKE_WORD_DETECTED");
        intent.putExtra("isWakeWordDetected", isWakeWordDetected);
        sendBroadcast(intent);
    }
    private void sendRecognizedSpeech(String recognizedSpeech) {
        Intent intent = new Intent("UPDATE_RECOGNIZED_SPEECH");
        intent.putExtra("recognizedSpeech", recognizedSpeech);
        sendBroadcast(intent);
    }
}