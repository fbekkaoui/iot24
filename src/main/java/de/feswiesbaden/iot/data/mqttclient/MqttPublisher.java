package de.feswiesbaden.iot.mqttclient;



import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;


public class MqttPublisher {

    private MqttClient client;
    private MyMqttCallback myCallback;


    public MqttPublisher(String mqttBroker, String clientName, MyMqttCallback myCallback)  {
 
        this.myCallback=myCallback;
        try {
             client=new MqttClient(UriResolver.Resolve(mqttBroker).toString(), clientName);
        
        } catch (MqttException | URISyntaxException | UnknownHostException e) {
            e.printStackTrace();
            //System.exit(1);
        }
    }

    public void start(String user, String pwd){

       
        client.setCallback(myCallback);

        MqttConnectOptions connOpts = new MqttConnectOptions();
	    connOpts.setCleanSession(true);//??
	    connOpts.setUserName(user);
        connOpts.setPassword(pwd.toCharArray());
     
        try {

            client.connect(connOpts);

        } catch (MqttException e){
            e.printStackTrace();
            //System.exit(1);
        }
        

    }

    public void subscribe(String topicFilter) {
        try {
            client.subscribe(topicFilter, 0);
        } catch (MqttException e){
            e.printStackTrace();
           //System.exit(1);
        }
    }

    public void publish(String topic,  String strMessage){
        
        MqttMessage message= new MqttMessage(strMessage.getBytes());
        
        try {
            client.publish(topic, message);
        } catch (MqttPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MqttException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}