package de.feswiesbaden.iot.mqttconnector;

import java.util.logging.Logger;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MyMqttCallback implements MqttCallback {

    private final Logger logger = Logger.getLogger(getClass().getName());
    private final MqttValueService mqttValueService;
    private final Runnable onMessageSaved;
    private final Runnable onConnectionLost;

    public MyMqttCallback(MqttValueService mqttValueService, Runnable onMessageSaved, Runnable onConnectionLost) {
        this.mqttValueService = mqttValueService;
        this.onMessageSaved = onMessageSaved;
        this.onConnectionLost = onConnectionLost;
        logger.info("Callback started");
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.info("Connection lost");
        onConnectionLost.run();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        logger.info("Delivery complete");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        MqttValue value = new MqttValue(message.toString(), topic);

        logger.info("Message arrived:");
        logger.info(value.toString());

        mqttValueService.save(value);
        onMessageSaved.run();
    }
}
