package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SpecsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        Intent intent = getIntent();
        // Initialize TextView for each data category. Update necessary.
        TextView name = findViewById(R.id.nameText);
        TextView processor = findViewById(R.id.processorText);
        TextView maxram = findViewById(R.id.maxramText);
        TextView year = findViewById(R.id.yearText);
        TextView model = findViewById(R.id.modelText);
        name.setText(intent.getStringExtra("name"));
        processor.setText(intent.getStringExtra("processor"));
        maxram.setText(intent.getStringExtra("maxram"));
        year.setText(intent.getStringExtra("year"));
        model.setText(intent.getStringExtra("model"));
    }
}
