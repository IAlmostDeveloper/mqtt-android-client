package com.example.almostdeveloper.mqttclient;

class MqttConnection {

    String mqttBrokerAddress;
    String username;
    String password;
    String rgbTopic;
    String displayTopic;
    String smallLedTopic;
    String sensorsTopic;
    String requestsTopic;

    MqttConnection(String mqttBrokerAddress, String username,
                   String password, String rgbTopic,
                   String displayTopic, String smallLedTopic,
                   String sensorsTopic, String requestsTopic) {

        this.mqttBrokerAddress = mqttBrokerAddress;
        this.username = username;
        this.password = password;
        this.rgbTopic = rgbTopic;
        this.displayTopic = displayTopic;
        this.smallLedTopic = smallLedTopic;
        this.sensorsTopic = sensorsTopic;
        this.requestsTopic = requestsTopic;
    }
}
