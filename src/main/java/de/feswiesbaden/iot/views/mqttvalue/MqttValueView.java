package de.feswiesbaden.iot.views.mqttvalue;

import java.util.logging.Logger;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
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
import de.feswiesbaden.iot.mqttconnector.MqttConnector;
import de.feswiesbaden.iot.mqttconnector.MyMqttCallback;
import de.feswiesbaden.iot.views.MainLayout;
import de.feswiesbaden.iot.views.MainViewController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

@PageTitle("Hello World")
@Route(value = "hello", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)

public class MqttValueView extends VerticalLayout {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private Grid<MqttValue> grid; //Grid für die Daten

    private MqttConnector mqttConnector;  //MQTT Publisher

    @Value("${mqtt.broker.address}")
    private String brokerAddress; //Adresse des Mqtt Brokers

    private final Environment env; //Umgebungsvariablen für die Konfiguration Mqtt Broker siehe application.properties

    MqttValueService mqttValueService; //Service für die Datenbank
    //private FeederThread thread; //Thread bsp, falls etwas im Hintergrund wiederholend durchgeführt werden soll

    /**
     * Wird aufgerufen, wenn die View angezeigt wird
     */
    @Override
    protected void onAttach(AttachEvent attachEvent) {

        //Singelton
        if(mqttConnector ==null){
            mqttConnector = new MqttConnector(brokerAddress, "Client-01",
                    new MyMqttCallback(mqttValueService, new MainViewController(attachEvent.getUI(), grid))
            );
            mqttConnector.start(
                    env.getProperty("mqtt.broker.username"),
                    env.getProperty("mqtt.broker.password")); //Verbindung zum Broker aufbauen
            mqttConnector.subscribe("#"); //abonniert alle Topics
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

    public MqttValueView(Environment env, MqttValueService mqttValueService) {
        this.env = env;

        this.mqttValueService=mqttValueService;

        add(new Span("Mqtt Broker Address: "+ brokerAddress));
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

        Button btnGridUpdater=new Button("Update Grid");
        btnGridUpdater.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGridUpdater.addClickListener(e-> {
            grid.setItems(mqttValueService.findAll());
        });

        btnPublish.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnPublish.addClickListener(e-> {
            MqttValue value = new MqttValue(tfMessage.getValue(), tfTopic.getValue());
            mqttConnector.publish(value);
            //grid.setItems(mqttValueService.findAll());
        });

        ePublish.setVerticalComponentAlignment(FlexComponent.Alignment.END, btnPublish);
        ePublish.add(tfTopic, tfMessage, btnPublish);

        add(ePublish, btnGridUpdater);

    }

    /**
     * Beispiel für die Anzeige der Daten in einem Grid
     */
    public void genExampleSubscribe(){

        grid = new Grid<>(MqttValue.class, false);
        grid.setItems(mqttValueService.findAll());

        grid.addColumn(MqttValue::getId).setHeader("id").setSortable(true);
        grid.addColumn(new LocalDateTimeRenderer<>(MqttValue::getTimeStamp,"dd.MM.YYYY HH:mm:ss"))
                .setHeader("Zeitstempel").setSortable(true).setComparator(MqttValue::getTimeStamp);
        grid.addColumn(MqttValue::getTopic).setHeader("Topic").setSortable(false);
        grid.addColumn(MqttValue::getMessage).setHeader("Message").setSortable(false).setFlexGrow(1);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event -> Notification.show(event.getItem().toString()));

        add(grid);
    }
}
