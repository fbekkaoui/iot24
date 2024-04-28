package de.feswiesbaden.iot.views.helloworld;

import java.util.LinkedList;
import java.util.List;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import de.feswiesbaden.iot.mqttpublisher.MqttPublisher;
import de.feswiesbaden.iot.mqttpublisher.MyMqttCallback;
import de.feswiesbaden.iot.views.MainLayout;
import de.feswiesbaden.iot.views.MainViewController;

@PageTitle("Hello World")
@Route(value = "hello", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class HelloWorldView extends VerticalLayout {

    private TextField name;
    private Button sayHello;

    private Grid<MqttValue> mqttGrid;
    private List<MqttValue> mqttValues; //Liste für die Daten
    private MqttPublisher publisher;  //MQTT Publisher
    private String brokerAdress="tcp://192.168.178.10:1883"; //Adresse des MQTT Brokers
    MqttValueService mqttValueService; //Service für die Datenbank


    /**
     * Wird aufgerufen, wenn die View angezeigt wird
     */
    @Override
     protected void onAttach(AttachEvent attachEvent) {

        //Singelton
        if(publisher==null){
            publisher = new MqttPublisher (brokerAdress, "Client-01",
                            new MyMqttCallback(mqttValues, 
                                new MainViewController(attachEvent.getUI(), mqttGrid)
                            )
                        );    
            publisher.start("user", "passwd");
            publisher.subscribe("#");
        }
    }
    
     /**
     * Wird aufgerufen, wenn die View nicht mehr angezeigt wird
     */
    @Override
     protected void onDetach(DetachEvent detachEvent) {
       
    }


    public HelloWorldView(MqttValueService mqttValueService) {
        
        name = new TextField("Your name");
        sayHello = new Button("Say hello");

        sayHello.addClickListener(e -> {
            Notification.show("Hello " + name.getValue());
        });
        sayHello.addClickShortcut(Key.ENTER);

        //MqttValue
        this.mqttValueService = mqttValueService;
        mqttValues = new LinkedList<>();//mqttValueService.findAll();

        mqttGrid = new Grid<>(MqttValue.class, false);
        mqttGrid.setItems(mqttValues);

        mqttGrid.addColumn(MqttValue::getId).setHeader("ID");
        mqttGrid.addColumn(MqttValue::getTimeStamp).setHeader("Timestamp");
        mqttGrid.addColumn(MqttValue::getTopic).setHeader("Topic");
        mqttGrid.addColumn(MqttValue::getMessage).setHeader("Message");


        setMargin(true);
        //setVerticalComponentAlignment(Alignment.END, name, sayHello);

        add(name, sayHello);
        add(mqttGrid);
    }

}
