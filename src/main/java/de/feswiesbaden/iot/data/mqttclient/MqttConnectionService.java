package de.feswiesbaden.iot.data.mqttclient;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import de.feswiesbaden.iot.mqttconnector.MqttConnector;
import de.feswiesbaden.iot.mqttconnector.MyMqttCallback;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class MqttConnectionService {

    private static final String DEFAULT_SUBSCRIPTION_TOPIC = "#";

    private final MqttValueService mqttValueService;
    private final List<Runnable> messageListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> connectionLostListeners = new CopyOnWriteArrayList<>();

    private MqttConnector mqttConnector;
    private String currentBrokerAddress;

    public MqttConnectionService(MqttValueService mqttValueService) {
        this.mqttValueService = mqttValueService;
    }

    public synchronized boolean connect(String brokerAddress, String user, String password) {
        disconnect();

        mqttConnector = new MqttConnector(
                brokerAddress,
                createClientId(),
                new MyMqttCallback(
                        mqttValueService,
                        this::notifyMessageSaved,
                        this::handleConnectionLost));

        boolean connected = mqttConnector.start(user, password);
        if (!connected) {
            mqttConnector = null;
            currentBrokerAddress = null;
            return false;
        }

        boolean subscribed = mqttConnector.subscribe(DEFAULT_SUBSCRIPTION_TOPIC);
        if (!subscribed) {
            disconnect();
            return false;
        }

        currentBrokerAddress = brokerAddress;
        return true;
    }

    public synchronized void disconnect() {
        if (mqttConnector != null) {
            mqttConnector.disconnect();
            mqttConnector = null;
        }
        currentBrokerAddress = null;
    }

    public synchronized boolean isConnected() {
        return mqttConnector != null && mqttConnector.isConnected();
    }

    public synchronized void publish(String topic, String message) {
        if (mqttConnector == null) {
            return;
        }
        mqttConnector.publish(topic, message);
    }

    public synchronized String getCurrentBrokerAddress() {
        return currentBrokerAddress;
    }

    public void addMessageListener(Runnable listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(Runnable listener) {
        messageListeners.remove(listener);
    }

    public void addConnectionLostListener(Runnable listener) {
        connectionLostListeners.add(listener);
    }

    public void removeConnectionLostListener(Runnable listener) {
        connectionLostListeners.remove(listener);
    }

    @PreDestroy
    public void shutdown() {
        disconnect();
    }

    private void notifyMessageSaved() {
        for (Runnable listener : messageListeners) {
            listener.run();
        }
    }

    private synchronized void handleConnectionLost() {
        mqttConnector = null;
        currentBrokerAddress = null;
        for (Runnable listener : connectionLostListeners) {
            listener.run();
        }
    }

    private String createClientId() {
        return "iot24-" + UUID.randomUUID();
    }
}
