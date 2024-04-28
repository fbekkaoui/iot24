package de.feswiesbaden.iot.views.mqttvalue;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
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
public class MqttValueView extends VerticalLayout {

    private final static Logger logger = Logger.getLogger(MqttValueView.class.getName());

    private Grid<MqttValue> grid; //Grid für die Daten
    private List<MqttValue> mqttValues; //Liste für die Daten
    private MqttPublisher publisher;  //MQTT Publisher
    private String brokerAdress="tcp://127.0.0.1:1883"; //Adresse des MQTT Brokers
    MqttValueService mqttValueService; //Service für die Datenbank
    //private FeederThread thread; //Thread bsp, falls etwas im Hintergrund wiederholend durchgeführt werden soll
    //static Logger logger = Logger.getLogger("MainView");


    /**
     * Wird aufgerufen, wenn die View angezeigt wird
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {

        //Singelton
        if(publisher==null){
            publisher = new MqttPublisher (brokerAdress, "Client-01",
                    new MyMqttCallback(mqttValues,
                            new MainViewController(attachEvent.getUI(), grid)
                    )
            );
            publisher.start("user", "passwd");
            publisher.subscribe("#");
        }

        logger.info("OnAttach!!");

        //TODO Thread bsp, falls etwas im Hintergrund wiederholend durchgeführt werden soll
        /*if(thread==null) {
            thread = new FeederThread(TODO thread was mitgeben?);
            thread.run();
        }
        */
    }

     /**
     * Wird aufgerufen, wenn die View nicht mehr angezeigt wird
     */
     @Override
     protected void onDetach(DetachEvent detachEvent) {
         // TODO document why this method is empty
         //thread.interrupt();
         //thread = null;
     }

    public MqttValueView(MqttValueService mqttValueService) {

        this.mqttValueService=mqttValueService;
        mqttValues = mqttValueService.findAll();

        add(new Span("Mqtt Broker Adress: "+brokerAdress));
        setSizeFull();
        genExamplePublish();
        genExampleSubscribe();
    }

    /**
     * Beispiel für die Veröffentlichung von Daten
     */
    public void genExamplePublish(){

        HorizontalLayout ePublish= new HorizontalLayout();

        Button btnPublish=new Button("Publish");
        TextField tfMessage = new TextField("Message");
        TextField tfTopic = new TextField("Topic");

        btnPublish.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnPublish.addClickListener(e-> {
            publisher.publish(tfTopic.getValue(), tfMessage.getValue());
            mqttValueService.update(new MqttValue(tfMessage.getValue(), tfTopic.getValue()));

        });

        ePublish.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnPublish);
        ePublish.add(tfTopic, tfMessage, btnPublish);

        add(ePublish);

    }

    /**
     * Beispiel für die Anzeige der Daten in einem Grid
     */
    public void genExampleSubscribe(){

        grid = new Grid<>(MqttValue.class, false);
        grid.setItems(mqttValues);

        grid.addColumn(MqttValue::getId).setHeader("id").setSortable(true);
        grid.addColumn(new LocalDateTimeRenderer<>(MqttValue::getTimeStamp,"dd.MM.YYYY HH:mm:ss"))
                .setHeader("Zeitstempel").setSortable(true).setComparator(MqttValue::getTimeStamp);
        grid.addColumn(MqttValue::getTopic).setHeader("Topic").setSortable(false);
        grid.addColumn(MqttValue::getMessage).setHeader("Message").setSortable(false);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event -> Notification.show(event.getItem().toString()));

        add(grid);

    }



}
