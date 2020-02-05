package com.ishaanohri.speedcheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String TAG = "INFO";
    long startTime;
    long endTime;
    long fileSize;
    OkHttpClient client = new OkHttpClient();

    static TextView speedTextView;
    static Button button;
    static CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        speedTextView = findViewById(R.id.speed);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setText(button, "Checking");
                setText(speedTextView, "");
                startTimer();
                Request request = new Request.Builder()
                        .url("https://images.app.goo.gl/UZVusEpJkD7KpLQRA")
                        .build();

                startTime = System.currentTimeMillis();

                client.newCall(request).enqueue(new Callback() {

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);

                        Headers responseHeaders = response.headers();
                        for (int i = 0, size = responseHeaders.size(); i < size; i++) {
                            Log.d(TAG, responseHeaders.name(i) + ": " + responseHeaders.value(i));
                        }

                        InputStream input = response.body().byteStream();

                        try {
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];

                            while (input.read(buffer) != -1) {
                                bos.write(buffer);
                            }
                            fileSize = bos.size();

                        } finally {
                            input.close();
                        }

                        endTime = System.currentTimeMillis();


                        double timeTakenMills = Math.floor(endTime - startTime);
                        double timeTakenInSecs = timeTakenMills / 1000;
                        final int kilobytePerSec = (int) Math.round(1024 / timeTakenInSecs);

                        double speed = fileSize / timeTakenMills;

                        Log.d(TAG, "Time taken in secs: " + timeTakenInSecs);
                        Log.d(TAG, "kilobyte per sec: " + kilobytePerSec);
                        Log.d(TAG, "Download Speed: " + speed);
                        Log.d(TAG, "File size: " + fileSize);

                        setText(speedTextView, kilobytePerSec + "KB/sec");
                        countDownTimer.cancel();

                        setText(button, "Check Speed");
                    }
                });
            }
        });
    }

    private void setText(final TextView text, final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public static void startTimer() {
        countDownTimer = new CountDownTimer(10 * 1000, 1000) {

            public void onTick(long millisUntilFinished) {
                Log.i("TAG",String.valueOf(millisUntilFinished));
            }

            public void onFinish() {
                if(speedTextView.getText().equals(""))
                {
                    speedTextView.setText("Poor/No Connection");
                    button.setText("Check Speed");
                }
            }
        }.start();
    }
}
