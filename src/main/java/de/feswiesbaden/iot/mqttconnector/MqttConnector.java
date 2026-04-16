package de.feswiesbaden.iot.mqttconnector;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Verwaltet MQTT-Verbindungen und behandelt das Versenden von Nachrichten mit
 * dem Eclipse Paho Client.
 */
public class MqttConnector {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final String brokerAddress;
    private final String clientName;
    private final MqttCallback callback;
    private MqttClient client;

    public MqttConnector(String brokerAddress, String clientName, MqttCallback callback) {
        this.brokerAddress = brokerAddress;
        this.clientName = clientName;
        this.callback = callback;
        initializeClient();
    }

    private void initializeClient() {
        try {
            client = new MqttClient(brokerAddress, clientName);
            client.setCallback(callback);
        } catch (MqttException e) {
            logger.severe("Fehler beim Initialisieren des MQTT-Clients: " + e.getMessage());
            client = null;
        }
    }

    public boolean start(String user, String password) {
        if (client == null) {
            initializeClient();
        }

        if (client == null) {
            return false;
        }

        MqttConnectOptions connectOptions = new MqttConnectOptions();
        connectOptions.setCleanSession(true);
        if (user != null && !user.isBlank()) {
            connectOptions.setUserName(user);
        }
        if (password != null && !password.isBlank()) {
            connectOptions.setPassword(password.toCharArray());
        }

        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.connect(connectOptions);
            logger.info("Verbindung zum MQTT-Broker erfolgreich hergestellt.");
            return true;
        } catch (MqttException e) {
            logger.severe("Verbindung zum MQTT-Broker konnte nicht hergestellt werden: " + e.getMessage());
            return false;
        }
    }

    public boolean subscribe(String topicFilter) {
        if (client == null || !client.isConnected()) {
            logger.warning("Abonnement nicht moeglich, da keine MQTT-Verbindung besteht.");
            return false;
        }

        try {
            client.subscribe(topicFilter, 0);
            logger.info("Erfolgreich auf Topic abonniert: " + topicFilter);
            return true;
        } catch (MqttException e) {
            logger.severe("Fehler beim Abonnieren des Topics: " + e.getMessage());
            return false;
        }
    }

    public void publish(String topic, String messageText) {
        if (client == null || !client.isConnected()) {
            logger.warning("Nachricht konnte nicht veroeffentlicht werden, da keine MQTT-Verbindung besteht.");
            return;
        }

        MqttMessage message = new MqttMessage(messageText.getBytes());
        try {
            client.publish(topic, message);
            logger.info("Nachricht veroeffentlicht: Topic = " + topic + ", Nachricht = " + messageText);
        } catch (MqttException e) {
            logger.severe("Fehler beim Veroeffentlichen der Nachricht: " + e.getMessage());
        }
    }

    public void publish(MqttValue mqttValue) {
        publish(mqttValue.getTopic(), mqttValue.getMessage());
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public void disconnect() {
        if (client == null || !client.isConnected()) {
            return;
        }

        try {
            client.disconnect();
            logger.info("MQTT-Verbindung getrennt.");
        } catch (MqttException e) {
            logger.warning("MQTT-Verbindung konnte nicht sauber getrennt werden: " + e.getMessage());
        }
    }
}
