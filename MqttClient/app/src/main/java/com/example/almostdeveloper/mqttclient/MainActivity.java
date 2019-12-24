package com.example.almostdeveloper.mqttclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences mqttSavedSettings;
    MqttAndroidClient mqttAndroidClient;
    MqttConnection mqttConnection;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttSavedSettings = getSharedPreferences("12345", Context.MODE_PRIVATE);
        actionBar = getSupportActionBar();
        mqttConnection = getSavedMqttConnection();
        setColorPicker();
        connectToMqtt();
        setListenerForSettingsButton();
    }

    private MqttConnection getSavedMqttConnection() {
        return new MqttConnection(
                mqttSavedSettings.getString("mqttBrokerAddress", "no value"),
                mqttSavedSettings.getString("username", "no value"),
                mqttSavedSettings.getString("password", "no value"),
                mqttSavedSettings.getString("redColorTopic", "no value"),
                mqttSavedSettings.getString("greenColorTopic", "no value"),
                mqttSavedSettings.getString("blueColorTopic", "no value"),
                mqttSavedSettings.getString("displayTopic", "no value"),
                mqttSavedSettings.getString("smallLedTopic", "no value")
        );
    }

    private void setListenerForSettingsButton() {
        findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNewMqttConnection();
            }
        });
    }

    private void getNewMqttConnection() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 0);
    }

    private void setColorPicker() {
        ColorPickerView colorPickerView = findViewById(R.id.color_picker_view);
        colorPickerView.addOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void onColorChanged(int selectedColor) {
                float A = (selectedColor >> 24) & 0xff;
                float multiplier = (A / 255) * 4;
                int R = (selectedColor >> 16) & 0xff;
                int G = (selectedColor >> 8) & 0xff;
                int B = (selectedColor) & 0xff;
                publishMessage((int) (R * multiplier), (int) (G * multiplier), (int) (B * multiplier));
            }
        });
    }

    private void connectToMqtt() {
        String clientId = "ExampleClientID" + System.currentTimeMillis();

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), mqttConnection.mqttBrokerAddress, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

                if (reconnect) {
                    Log.d("1234", "Reconnected to : " + serverURI);
                } else {
                    Log.d("1234", "Connected to: " + serverURI);
                }
                actionBar.setTitle("MqttClient(Connected)");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.d("1234", "The Connection was lost.");
                actionBar.setTitle("MqttClient(Not connected)");

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Log.d("1234", "Incoming message: " + new String(message.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });

        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(mqttConnection.username);
        mqttConnectOptions.setPassword(mqttConnection.password.toCharArray());


        try {
            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    actionBar.setTitle("MqttClient(Connected)");

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d("1234", "Failed to connect to: " + mqttConnection.mqttBrokerAddress);
                    actionBar.setTitle("MqttClient(Not connected)");
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }

    }

    public void publishMessage(int R, int G, int B) {

        try {
            MqttMessage rmessage = new MqttMessage();
            MqttMessage gmessage = new MqttMessage();
            MqttMessage bmessage = new MqttMessage();
            rmessage.setPayload(String.valueOf(R).getBytes());
            gmessage.setPayload(String.valueOf(G).getBytes());
            bmessage.setPayload(String.valueOf(B).getBytes());
            mqttAndroidClient.publish(mqttConnection.redColorTopic, rmessage);
            mqttAndroidClient.publish(mqttConnection.greenColorTopic, gmessage);
            mqttAndroidClient.publish(mqttConnection.blueColorTopic, bmessage);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            if (data != null) {
                mqttSavedSettings
                    .edit()
                    .putString("mqttBrokerAddress", data.getStringExtra("mqttBrokerAddress"))
                    .putString("username", data.getStringExtra("username"))
                    .putString("password", data.getStringExtra("password"))
                    .putString("redColorTopic", data.getStringExtra("redColorTopic"))
                    .putString("greenColorTopic", data.getStringExtra("greenColorTopic"))
                    .putString("blueColorTopic", data.getStringExtra("blueColorTopic"))
                    .putString("displayTopic", data.getStringExtra("displayTopic"))
                    .putString("smallLedTopic", data.getStringExtra("smallLedTopic"))
                    .apply();
                mqttConnection = getSavedMqttConnection();
                connectToMqtt();
            }
    }
}
