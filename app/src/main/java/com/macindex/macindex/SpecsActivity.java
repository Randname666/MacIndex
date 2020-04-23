package com.macindex.macindex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.File;

public class SpecsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specs);
        try {
            Intent intent = getIntent();
            this.setTitle(intent.getStringExtra("name"));
            // Initialize TextView for each data category. Update necessary.
            TextView name = findViewById(R.id.nameText);
            TextView processor = findViewById(R.id.processorText);
            TextView maxram = findViewById(R.id.maxramText);
            TextView year = findViewById(R.id.yearText);
            TextView model = findViewById(R.id.modelText);
            ImageView image = findViewById(R.id.pic);

            name.setText(intent.getStringExtra("name"));
            processor.setText(intent.getStringExtra("processor"));
            maxram.setText(intent.getStringExtra("maxram"));
            year.setText(intent.getStringExtra("year"));
            model.setText(intent.getStringExtra("model"));

            final MediaPlayer sound = MediaPlayer.create(this,
                    SoundHelper.getSound(intent.getStringExtra("sound")));

            String path = intent.getStringExtra("path");
            File file = new File(path);
            if (file.exists()) {
                Log.i("SpecsAct", "Image exists");
                image.setImageBitmap(BitmapFactory.decodeFile(file.getPath()));
            }
            file.delete();

            image.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View unused) {
                    sound.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            new AlertDialog.Builder(this)
                    .setMessage(this.getResources().getString(R.string.err_intent_invalid))
                    .setNegativeButton(this.getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            finishAffinity();
                        } })
                    .setTitle(this.getResources().getString(R.string.error)).setCancelable(false).show();
        }
    }
}
