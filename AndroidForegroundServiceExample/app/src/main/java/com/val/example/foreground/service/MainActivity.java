package com.val.example.foreground.service;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button startButton,stopButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startButton= (Button)findViewById(R.id.button1);
        stopButton = (Button)findViewById(R.id.button2);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(MainActivity.this, ForegroundService.class);
                startIntent.setAction(Constantes.STARTFOREGROUND_ACTION);
                startService(startIntent);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent stopIntent = new Intent(MainActivity.this, ForegroundService.class);
                stopIntent.setAction(Constantes.STOPFOREGROUND_ACTION);
                startService(stopIntent);
            }
        });



    }
}
