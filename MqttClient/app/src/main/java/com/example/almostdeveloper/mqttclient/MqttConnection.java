package com.example.almostdeveloper.mqttclient;

class MqttConnection {

    String mqttBrokerAddress;
    String username;
    String password;
    String rgbTopic;
    String displayTopic;
    String smallLedTopic;

    MqttConnection(String mqttBrokerAddress, String username,
                   String password, String rgbTopic,
                   String displayTopic, String smallLedTopic) {

        this.mqttBrokerAddress = mqttBrokerAddress;
        this.username = username;
        this.password = password;
        this.rgbTopic = rgbTopic;
        this.displayTopic = displayTopic;
        this.smallLedTopic = smallLedTopic;
    }
}
