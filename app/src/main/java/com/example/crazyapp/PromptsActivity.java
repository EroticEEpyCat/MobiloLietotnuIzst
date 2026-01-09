package com.example.crazyapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PromptsActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> promptList;
    ArrayAdapter<String> adapter;
    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompts);

        listView = findViewById(R.id.listView);
        db = new DBHelper(this);
        promptList = new ArrayList<>();

        Cursor cursor = db.getAllPrompts();
        if (cursor.moveToFirst()) {
            do {
                String prompt = cursor.getString(cursor.getColumnIndexOrThrow("prompt"));
                String answer = cursor.getString(cursor.getColumnIndexOrThrow("answer"));
                promptList.add("Prompt: " + prompt + "\nAnswer: " + answer);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, R.layout.row, promptList);
        listView.setAdapter(adapter);
    }
}
