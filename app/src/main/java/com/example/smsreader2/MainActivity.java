package com.example.smsreader2;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);
    private Button initiateRead;
    private TextView textSms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiateRead = findViewById(R.id.reader);
        textSms = findViewById(R.id.textSms);

        initiateRead.setOnClickListener(this);
        LOGGER.info("****Started****");
    }

    public void sms() {
        LOGGER.info("Into sms!");
        Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);

        if (cursor.moveToFirst()) { // must check the result to prevent exception
            List<String> data = new ArrayList<>();
            StringBuilder text = new StringBuilder();
            do {
                StringBuilder msgData = new StringBuilder();
                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {
                    msgData.append(" ").append(cursor.getColumnName(idx)).append(":").append(cursor.getString(idx));
                }
                data.add(msgData.toString());
                // use msgData
            } while (cursor.moveToNext());
            data.forEach(val -> {
                LOGGER.info("Data read: [{}]", val);
                text.append(val).append("\n").append("\n");
            });

            File folder = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/SMS");
            folder.mkdirs();
            String externalFileLocation = folder.getAbsolutePath() + "/sms.backup." + Instant.now() + ".txt";
            externalFileLocation = externalFileLocation.replaceAll(":", "-");
            try (PrintWriter printWriter = new PrintWriter(new FileWriter(new File(externalFileLocation), false))) {
                printWriter.write(text.toString());
                printWriter.close();
                LOGGER.info("File writing complete at {}", externalFileLocation);
            } catch (IOException e) {
                LOGGER.error("", e);
            }
            textSms.setText("File writing complete at " + externalFileLocation);
        } else {
            // empty box, no SMS
            LOGGER.warn("No SMS to read!");
        }
        cursor.close();
    }

    @Override
    public void onClick(View view) {
        if (view == initiateRead)
            sms();
    }
}