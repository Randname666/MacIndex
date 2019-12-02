package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * MacIndex Android application and Specs database
 * University of Illinois, CS125 FA19 Final Project
 *
 * For additional Database Design Information, please refer to:
 * https://github.com/paizhangpi/MacIndex/
 */
public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatabase();
        initInterface();
    }

    private void initDatabase() {
        String appDataPath = this.getApplicationInfo().dataDir;
        File dbFolder = new File(appDataPath + "/databases");
        dbFolder.mkdir();
        File dbFilePath = new File(appDataPath + "/databases/specs.db");
        try {
            InputStream inputStream = this.getAssets().open("specs.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0)
            {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e){
            // To catch
        }
        DatabaseOpenHelper dbHelper = new DatabaseOpenHelper(this);
        database = dbHelper.getReadableDatabase();
    }

    /**
     * If new category, or data category was added, database structure was changed.
     * Thus, code update is necessary to handle new data. Please refer to:
     * https://github.com/paizhangpi/MacIndex/
     *
     * Not necessary if only new machine was added.
     */
    private void initInterface() {
        // Change the number below.
        for (int i = 0; i <= 9; i++) {
            LinearLayout currentLayout = findViewById(CategoryHelper.getLayout(i));
            initCategory(currentLayout, i);
        }
        // Basic functionality was finished on 16:12 CST, Dec 2, 2019.
    }

    private void initCategory(LinearLayout currentLayout, int category) {
        Cursor cursor = database.query("category" + String.valueOf(category), null,
                null, null, null, null, null);
        while (cursor.moveToNext()) {
            View mainChunk = getLayoutInflater().inflate(R.layout.chunk_main, null);
            TextView machineName = mainChunk.findViewById(R.id.machineName);
            Button viewButton = mainChunk.findViewById(R.id.viewButton);
            // Create a String for each data category. Update here.
            final String thisName = cursor.getString(cursor.getColumnIndex("name"));
            final String thisProcessor = cursor.getString(cursor.getColumnIndex("processor"));
            final String thisMaxRAM = cursor.getString(cursor.getColumnIndex("maxram"));
            final String thisYear = cursor.getString(cursor.getColumnIndex("year"));
            final String thisModel = cursor.getString(cursor.getColumnIndex("model"));
            machineName.setText(thisName);
            viewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View unused) {
                    Intent intent = new Intent(MainActivity.this, SpecsActivity.class);
                    // Put each String to Specs Intent. Update here.
                    intent.putExtra("name", thisName);
                    intent.putExtra("processor", thisProcessor);
                    intent.putExtra("maxram", thisMaxRAM);
                    intent.putExtra("year", thisYear);
                    intent.putExtra("model", thisModel);
                    startActivity(intent);
                }
            });
            currentLayout.addView(mainChunk);
        }
    }
}
