package de.feswiesbaden.iot.data.mqttclient;

import java.time.LocalDateTime;

import de.feswiesbaden.iot.data.AbstractEntity;
import jakarta.persistence.Entity;

@Entity
public class MqttValue extends AbstractEntity {
    private String message;
    private String topic;
    private LocalDateTime timeStamp;

    public MqttValue() {
    }

    public MqttValue(String message, String topic) {
        this.message = message;
        this.topic = topic;
        this.timeStamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(LocalDateTime timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "MqttValue [message=" + message + ", timeStamp=" + timeStamp + ", topic=" + topic + "]";
    }
}
