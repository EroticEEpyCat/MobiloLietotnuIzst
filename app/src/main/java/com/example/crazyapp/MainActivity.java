package com.example.crazyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    EditText grade1, grade2, grade3;
    Button calcButton;
    TextView resultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        grade1 = findViewById(R.id.grade1);
        grade2 = findViewById(R.id.grade2);
        grade3 = findViewById(R.id.grade3);
        calcButton = findViewById(R.id.calcButton);
        resultText = findViewById(R.id.resultText);

        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int g1 = Integer.parseInt(grade1.getText().toString());
                int g2 = Integer.parseInt(grade2.getText().toString());
                int g3 = Integer.parseInt(grade3.getText().toString());

                double average = (g1 + g2 + g3) / 3.0;

                resultText.setText("Vidējā atzīme: " + average);
            }
        });
    }
}
