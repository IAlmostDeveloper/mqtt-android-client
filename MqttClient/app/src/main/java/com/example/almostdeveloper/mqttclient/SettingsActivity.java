package com.example.almostdeveloper.mqttclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences mqttSavedSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mqttSavedSettings = getSharedPreferences("12345", Context.MODE_PRIVATE);
        setSaveButtonListener();
        setInputFields();
    }

    private void setInputFields() {
        ((EditText) findViewById(R.id.mqttBrokerAddress_input)).setText(mqttSavedSettings.getString("mqttBrokerAddress", "no value"));
        ((EditText) findViewById(R.id.username_input)).setText(mqttSavedSettings.getString("username", "no value"));
        ((EditText) findViewById(R.id.password_input)).setText(mqttSavedSettings.getString("password", "no value"));
        ((EditText) findViewById(R.id.redColorTopic_input)).setText(mqttSavedSettings.getString("redColorTopic", "no value"));
        ((EditText) findViewById(R.id.greenColorTopic_input)).setText(mqttSavedSettings.getString("greenColorTopic", "no value"));
        ((EditText) findViewById(R.id.blueColorTopic_input)).setText(mqttSavedSettings.getString("blueColorTopic", "no value"));
        ((EditText) findViewById(R.id.displayTopic_input)).setText(mqttSavedSettings.getString("displayTopic", "no value"));
        ((EditText) findViewById(R.id.smallLedTopic_input)).setText(mqttSavedSettings.getString("smallLedTopic", "no value"));
    }

    private void setSaveButtonListener() {
        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent()
                        .putExtra("mqttBrokerAddress", ((EditText) findViewById(R.id.mqttBrokerAddress_input)).getText().toString())
                        .putExtra("username", ((EditText) findViewById(R.id.username_input)).getText().toString())
                        .putExtra("password", ((EditText) findViewById(R.id.password_input)).getText().toString())
                        .putExtra("redColorTopic", ((EditText) findViewById(R.id.redColorTopic_input)).getText().toString())
                        .putExtra("greenColorTopic", ((EditText) findViewById(R.id.greenColorTopic_input)).getText().toString())
                        .putExtra("blueColorTopic", ((EditText) findViewById(R.id.blueColorTopic_input)).getText().toString())
                        .putExtra("displayTopic", ((EditText) findViewById(R.id.displayTopic_input)).getText().toString())
                        .putExtra("smallLedTopic", ((EditText) findViewById(R.id.smallLedTopic_input)).getText().toString());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
