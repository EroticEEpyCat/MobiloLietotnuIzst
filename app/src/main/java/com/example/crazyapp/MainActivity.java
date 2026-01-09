package com.example.crazyapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;

    EditText grade1, grade2, grade3;
    Button calcButton, cameraButton;
    TextView resultText;

    EditText participantsInput;
    Button fetchActivityButton;
    TextView activityResultText, apiEndpointText;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind UI elements
        grade1 = findViewById(R.id.grade1);
        grade2 = findViewById(R.id.grade2);
        grade3 = findViewById(R.id.grade3);
        calcButton = findViewById(R.id.calcButton);
        cameraButton = findViewById(R.id.cameraButton);
        resultText = findViewById(R.id.resultText);

        participantsInput = findViewById(R.id.participantsInput);
        fetchActivityButton = findViewById(R.id.fetchActivityButton);
        activityResultText = findViewById(R.id.activityResultText);
        apiEndpointText = findViewById(R.id.apiEndpointText);

        // Grade calculation
        calcButton.setOnClickListener(v -> {
            try {
                int g1 = Integer.parseInt(grade1.getText().toString());
                int g2 = Integer.parseInt(grade2.getText().toString());
                int g3 = Integer.parseInt(grade3.getText().toString());

                double average = (g1 + g2 + g3) / 3.0;
                resultText.setText("Vidējā atzīme: " + average);
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "Lūdzu, ievadiet visas atzīmes", Toast.LENGTH_SHORT).show();
            }
        });

        // Camera functionality
        cameraButton.setOnClickListener(v -> {
            if (checkPermissions()) {
                openCamera();
            } else {
                requestPermissions();
            }
        });

        // Update endpoint preview dynamically
        participantsInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String participants = s.toString().trim();
                apiEndpointText.setText("API Endpoint: https://bored-api.appbrewery.com/filter?participants=" + participants);
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Fetch activity from App Brewery API
        fetchActivityButton.setOnClickListener(v -> {
            String participantsStr = participantsInput.getText().toString().trim();
            if (participantsStr.isEmpty()) {
                Toast.makeText(MainActivity.this, "Ievadiet dalībnieku skaitu", Toast.LENGTH_SHORT).show();
                return;
            }
            int participants = Integer.parseInt(participantsStr);
            fetchActivity(participants);
        });
    }

    // Check camera permission
    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    // Request camera permission
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open camera and save image to gallery
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "Attēls saglabāts galerijā!", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch activity from App Brewery API
    private void fetchActivity(int participants) {
        String urlString = "https://bored-api.appbrewery.com/filter?participants=" + participants;

        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... urls) {
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);
                    conn.connect();

                    int responseCode = conn.getResponseCode();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            responseCode == HttpURLConnection.HTTP_OK ? conn.getInputStream() : conn.getErrorStream()
                    ));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();

                    // Log the raw response
                    android.util.Log.d("API_DEBUG", "Raw JSON: " + result.toString());

                    JSONArray jsonArray = new JSONArray(result.toString());

                    if (jsonArray.length() == 0) {
                        return "Nav aktivitāšu šim dalībnieku skaitam";
                    }

                    // Return the first activity
                    JSONObject firstActivity = jsonArray.getJSONObject(0);
                    return firstActivity.getString("activity");

                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("API_DEBUG", "Exception: ", e);
                    return "Kļūda: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String activity) {
                activityResultText.setText(activity);
            }
        }.execute(urlString);
    }
}
