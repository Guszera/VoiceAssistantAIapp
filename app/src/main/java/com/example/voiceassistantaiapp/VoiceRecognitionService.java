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

public class VoiceRecognitionService extends Service {

    private SpeechRecognizer speechRecognizer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startListening();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // This service is not designed with binding in mind.
        return null;
    }

    private void startListening() {
        Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d("VoiceRecognitionService", "onReadyForSpeech");
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d("VoiceRecognitionService", "onBeginningOfSpeech");
                }

                @Override
                public void onRmsChanged(float rmsdB) {}

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    Log.d("VoiceRecognitionService", "onEndOfSpeech");
                }

                @Override
                public void onError(int error) {
                    Log.e("VoiceRecognitionService", "Error: " + error);
                    startListening(); // Restart listening on error
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        Log.d("VoiceRecognitionService", "onResults: " + recognizedText);

                        Intent intent = new Intent("ACTION_RECOGNIZED_TEXT");
                        intent.putExtra("recognizedText", recognizedText);

                        // Example: Checking for specific phrases
                        if (recognizedText.equalsIgnoreCase("open settings")) {
                            intent.putExtra("command", "openSettings");
                        } else if (recognizedText.equalsIgnoreCase("what's the time")) {
                            intent.putExtra("command", "tellTime");
                        } else if (recognizedText.equalsIgnoreCase("find the nearest petrol station")) {
                            intent.putExtra("command", "findPetrolStation");
                        }

                        // You can add more else-if branches for other phrases

                        sendBroadcast(intent);
                    }
                    startListening(); // Continuous listening
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        String partialText = matches.get(0);
                        Log.d("VoiceRecognitionService", "onPartialResults: " + partialText);
                        broadcastUpdate(partialText);
                    }
                }

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        }
        speechRecognizer.startListening(recognizerIntent);
    }
    private void broadcastUpdate(String recognizedText) {
        Log.d("VoiceService", "Broadcasting detected text: " + recognizedText); // Log for debugging
        Intent intent = new Intent("ACTION_RECOGNIZED_TEXT");
        intent.putExtra("recognizedText", recognizedText);
        sendBroadcast(intent);
    }
    @Override
    public void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        super.onDestroy();
    }
}
