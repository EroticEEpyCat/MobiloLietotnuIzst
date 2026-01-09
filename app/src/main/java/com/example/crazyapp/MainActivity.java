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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int PERMISSION_REQUEST_CODE = 200;

    EditText grade1, grade2, grade3;
    Button calcButton, cameraButton;
    TextView resultText;

    EditText promptInput;
    Button aiButton;
    TextView aiResultText;

    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grade1 = findViewById(R.id.grade1);
        grade2 = findViewById(R.id.grade2);
        grade3 = findViewById(R.id.grade3);
        calcButton = findViewById(R.id.calcButton);
        cameraButton = findViewById(R.id.cameraButton);
        resultText = findViewById(R.id.resultText);

        promptInput = findViewById(R.id.participantsInput);
        aiButton = findViewById(R.id.fetchActivityButton);
        aiResultText = findViewById(R.id.activityResultText);

        calcButton.setOnClickListener(v -> {
            try {
                int g1 = Integer.parseInt(grade1.getText().toString());
                int g2 = Integer.parseInt(grade2.getText().toString());
                int g3 = Integer.parseInt(grade3.getText().toString());
                double average = (g1 + g2 + g3) / 3.0;
                resultText.setText("AVERAGE GRADE: " + average);
            } catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this, "FILL IN ALL THE GRADES", Toast.LENGTH_SHORT).show();
            }
        });

        cameraButton.setOnClickListener(v -> {
            if (checkPermissions()) openCamera();
            else requestPermissions();
        });

        aiButton.setOnClickListener(v -> {
            String prompt = promptInput.getText().toString().trim();
            if (prompt.isEmpty()) {
                Toast.makeText(MainActivity.this, "ENTER A PROMPT", Toast.LENGTH_SHORT).show();
            } else {
                callFreeAI(prompt);
            }
        });
    }

    private boolean checkPermissions() {
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) openCamera();
            else Toast.makeText(this, "NEED CAMERA PERMISSION", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "PIC");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            Toast.makeText(this, "Pic saved in gallery!", Toast.LENGTH_SHORT).show();
    }

    private void callFreeAI(String prompt) {
        new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL("https://apifreellm.com/api/chat");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json; utf-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);

                    JSONObject body = new JSONObject();
                    body.put("message", prompt);

                    OutputStream os = conn.getOutputStream();
                    os.write(body.toString().getBytes("UTF-8"));
                    os.close();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    conn.getResponseCode() == HttpURLConnection.HTTP_OK
                                            ? conn.getInputStream()
                                            : conn.getErrorStream()
                            )
                    );

                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) response.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(response.toString());
                    return json.optString("response", "No response");

                } catch (Exception e) {
                    e.printStackTrace();
                    return "Error: " + e.getMessage();
                }
            }

            @Override
            protected void onPostExecute(String result) {
                aiResultText.setText(result);

                DBHelper db = new DBHelper(MainActivity.this);
                db.insertPrompt(promptInput.getText().toString(), result);

                Intent intent = new Intent(MainActivity.this, PromptsActivity.class);
                startActivity(intent);
            }
        }.execute();
    }
}
