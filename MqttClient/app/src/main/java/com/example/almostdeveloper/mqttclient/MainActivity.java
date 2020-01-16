package com.example.almostdeveloper.mqttclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorChangedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    SharedPreferences mqttSavedSettings;
    MqttAndroidClient mqttAndroidClient;
    MqttConnection mqttConnection;
    ActionBar actionBar;
    Context context;
    TextView humiditySensor;
    TextView temperatureSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttSavedSettings = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        actionBar = getSupportActionBar();
        mqttConnection = getSavedMqttConnection();
        context = this;
        setSensorsTextViews();
        setDialogButtonListener();
        setSendButtonListener();
        setSettingsButtonListener();
        setRefreshSensorsDataButtonListener();
        setBuiltInLedCheckBoxListener();
        connectToMqtt();
    }

    private void setSensorsTextViews() {
        temperatureSensor = findViewById(R.id.temperature_sensor_text);
        humiditySensor = findViewById(R.id.humidity_sensor_text);
    }

    private void setRefreshSensorsDataButtonListener() {
        findViewById(R.id.refresh_sensors_data_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mqttAndroidClient.publish(mqttConnection.requestsTopic, new MqttMessage());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void setBuiltInLedCheckBoxListener() {
        ((CheckBox) findViewById(R.id.built_in_led_checkbox)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                publishBuiltInLedMessage(isChecked ? 0 : 1);
            }
        });
    }

    private void setSendButtonListener() {
        findViewById(R.id.send_text_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = findViewById(R.id.text_input);
                String text = input.getText().toString();
                if (!text.equals("")) {
                    publishTextMessage(text);
                    input.setText("");
                }
            }
        });
    }

    private void setDialogButtonListener() {
        findViewById(R.id.change_color_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ColorPickerDialogBuilder
                        .with(context)
                        .setTitle("Choose color")
                        .setOnColorChangedListener(new OnColorChangedListener() {
                            @Override
                            public void onColorChanged(int selectedColor) {
                                float A = (selectedColor >> 24) & 0xff;
                                float multiplier = (A / 255) * 4;
                                int R = (selectedColor >> 16) & 0xff;
                                int G = (selectedColor >> 8) & 0xff;
                                int B = (selectedColor) & 0xff;
                                publishRGBMessage((int) (R * multiplier), (int) (G * multiplier), (int) (B * multiplier));
                            }
                        })
                        .initialColor(Color.parseColor("#000000"))
                        .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
                        .density(10)
                        .setPositiveButton("ok", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });
    }

    private MqttConnection getSavedMqttConnection() {
        return new MqttConnection(
                mqttSavedSettings.getString("mqttBrokerAddress", "no value"),
                mqttSavedSettings.getString("username", "no value"),
                mqttSavedSettings.getString("password", "no value"),
                mqttSavedSettings.getString("rgbTopic", "no value"),
                mqttSavedSettings.getString("displayTopic", "no value"),
                mqttSavedSettings.getString("smallLedTopic", "no value"),
                mqttSavedSettings.getString("sensorsTopic", "no value"),
                mqttSavedSettings.getString("requestsTopic", "no value")
        );
    }

    private void setSettingsButtonListener() {
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
            public void messageArrived(String topic, MqttMessage message) {
                Log.d("1234", "Incoming message: " + new String(message.getPayload()));
                refreshSensorsDataView(message.toString());
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
                    try {
                        mqttAndroidClient.subscribe(mqttConnection.sensorsTopic, 2, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Toast.makeText(getApplicationContext(), "subscribe success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Toast.makeText(getApplicationContext(), "subscribe failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
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

    private void refreshSensorsDataView(String message) {
        try {
            JSONObject jsonObject = new JSONObject(message);
            String temperatureSensorState = jsonObject.get("temperature").toString();
            String humiditySensorState = jsonObject.get("humidity").toString();
            temperatureSensor.setText("Temperature : " + temperatureSensorState + " °C");
            humiditySensor.setText("Humidity: " + humiditySensorState + " %");
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void publishRGBMessage(int R, int G, int B) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("R", String.valueOf(R));
            jsonObject.put("G", String.valueOf(G));
            jsonObject.put("B", String.valueOf(B));
            MqttMessage mqttMessage = new MqttMessage();
            mqttMessage.setPayload(jsonObject.toString().getBytes());
            mqttAndroidClient.publish(mqttConnection.rgbTopic, mqttMessage);
        } catch (JSONException | MqttException ignored) {

        }
    }

    public void publishTextMessage(String text) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(text.getBytes());
        try {
            mqttAndroidClient.publish(mqttConnection.displayTopic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publishBuiltInLedMessage(int state) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(String.valueOf(state).getBytes());
        try {
            mqttAndroidClient.publish(mqttConnection.smallLedTopic, mqttMessage);
        } catch (MqttException e) {
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
                        .putString("rgbTopic", data.getStringExtra("rgbTopic"))
                        .putString("displayTopic", data.getStringExtra("displayTopic"))
                        .putString("smallLedTopic", data.getStringExtra("smallLedTopic"))
                        .putString("sensorsTopic", data.getStringExtra("sensorsTopic"))
                        .putString("requestsTopic", data.getStringExtra("requestsTopic"))
                        .apply();
                mqttConnection = getSavedMqttConnection();
                connectToMqtt();
            }
    }
}
