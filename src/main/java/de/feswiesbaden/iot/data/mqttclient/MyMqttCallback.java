package de.feswiesbaden.iot.mqttclient;


import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import de.feswiesbaden.iot.views.MainViewController;

public class MyMqttCallback implements MqttCallback  {

    List<MqttValue> mqttValues;

    private MainViewController mainViewController;
   

    public MyMqttCallback(List<MqttValue> mqttValues, MainViewController mainViewController){
        
        this.mainViewController=mainViewController;
        this.mqttValues=mqttValues;

        System.out.println("Callback started!!");
    }
    @Override
    public void connectionLost(Throwable cause) {
        // TODO Auto-generated method stub
         
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // TODO Auto-generated method stub
        
        System.out.println("Delivery complete");
        
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // TODO Auto-generated method stub


        MqttValue value =new MqttValue(message.toString(), topic);
        System.out.println(value.toString());

        mqttValues.add(value);
        mainViewController.updateGrid();
    
        
    }
    
}

