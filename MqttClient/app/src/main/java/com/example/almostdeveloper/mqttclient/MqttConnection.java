package com.example.almostdeveloper.mqttclient;

class MqttConnection {

    String mqttBrokerAddress;
    String username;
    String password;
    String redColorTopic;
    String greenColorTopic;
    String blueColorTopic;
    String displayTopic;
    String smallLedTopic;

    MqttConnection(String mqttBrokerAddress, String username,
                   String password, String redColorTopic,
                   String greenColorTopic, String blueColorTopic,
                   String displayTopic, String smallLedTopic) {

        this.mqttBrokerAddress = mqttBrokerAddress;
        this.username = username;
        this.password = password;
        this.redColorTopic = redColorTopic;
        this.greenColorTopic = greenColorTopic;
        this.blueColorTopic = blueColorTopic;
        this.displayTopic = displayTopic;
        this.smallLedTopic = smallLedTopic;
    }
}
