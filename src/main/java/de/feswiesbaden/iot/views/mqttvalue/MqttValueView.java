package de.feswiesbaden.iot.views.mqttvalue;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.feswiesbaden.iot.data.mqttclient.MqttConnectionService;
import de.feswiesbaden.iot.data.mqttclient.MqttValue;
import de.feswiesbaden.iot.data.mqttclient.MqttValueService;
import de.feswiesbaden.iot.views.MainLayout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

@PageTitle("MQTT")
@Route(value = "mqtt", layout = MainLayout.class)
public class MqttValueView extends VerticalLayout {

    private final Environment env;
    private final MqttValueService mqttValueService;
    private final MqttConnectionService mqttConnectionService;
    private final TextField brokerAddressField = new TextField("MQTT Broker Address");
    private final Span connectionStatus = new Span("Nicht verbunden");
    private final Runnable messageListener = this::refreshGrid;
    private final Runnable connectionLostListener = this::showConnectionLost;

    private Grid<MqttValue> grid;

    public MqttValueView(Environment env, MqttValueService mqttValueService, MqttConnectionService mqttConnectionService,
            @Value("${mqtt.broker.address}") String defaultBrokerAddress) {
        this.env = env;
        this.mqttValueService = mqttValueService;
        this.mqttConnectionService = mqttConnectionService;

        setSizeFull();
        setSpacing(true);
        setPadding(true);
        add(new H2("MQTT Arbeitsbereich"));
        configureConnectionControls(defaultBrokerAddress);
        configurePublishControls();
        configureGrid();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        mqttConnectionService.addMessageListener(messageListener);
        mqttConnectionService.addConnectionLostListener(connectionLostListener);
        refreshConnectionStatus();
        refreshGrid();
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        mqttConnectionService.removeMessageListener(messageListener);
        mqttConnectionService.removeConnectionLostListener(connectionLostListener);
    }

    private void configureConnectionControls(String defaultBrokerAddress) {
        brokerAddressField.setWidth("22rem");
        brokerAddressField.setValue(defaultBrokerAddress);

        Button connectButton = new Button("Verbinden");
        connectButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        connectButton.addClickListener(event -> connectToBroker());

        Button disconnectButton = new Button("Trennen");
        disconnectButton.addClickListener(event -> disconnectFromBroker());

        HorizontalLayout connectionLayout = new HorizontalLayout(
                brokerAddressField,
                connectButton,
                disconnectButton,
                connectionStatus);
        connectionLayout.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        add(connectionLayout);
    }

    private void configurePublishControls() {
        HorizontalLayout publishLayout = new HorizontalLayout();

        Button publishButton = new Button("Publish");
        TextField messageField = new TextField("Message");
        TextField topicField = new TextField("Topic");

        publishButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        publishButton.addClickListener(event -> publishMessage(topicField.getValue(), messageField.getValue()));

        publishLayout.setVerticalComponentAlignment(FlexComponent.Alignment.END, publishButton);
        publishLayout.add(topicField, messageField, publishButton);
        add(publishLayout);
    }

    private void configureGrid() {
        grid = new Grid<>(MqttValue.class, false);
        grid.addColumn(MqttValue::getId).setHeader("id").setSortable(true);
        grid.addColumn(new LocalDateTimeRenderer<>(MqttValue::getTimeStamp, "dd.MM.yyyy HH:mm:ss"))
                .setHeader("Zeitstempel").setSortable(true).setComparator(MqttValue::getTimeStamp);
        grid.addColumn(MqttValue::getTopic).setHeader("Topic");
        grid.addColumn(MqttValue::getMessage).setHeader("Message").setFlexGrow(1);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event -> Notification.show(event.getItem().toString()));

        refreshGrid();
        add(grid);
    }

    private void connectToBroker() {
        String brokerAddress = brokerAddressField.getValue() == null ? "" : brokerAddressField.getValue().trim();
        if (brokerAddress.isEmpty()) {
            showDisconnected("Bitte eine Broker-Adresse eingeben");
            Notification.show("Bitte eine Broker-Adresse eingeben");
            return;
        }

        boolean connected = mqttConnectionService.connect(
                brokerAddress,
                env.getProperty("mqtt.broker.username"),
                env.getProperty("mqtt.broker.password"));

        if (connected) {
            showConnected(brokerAddress);
            Notification.show("Verbindung zum MQTT-Broker hergestellt");
        } else {
            showDisconnected("Verbindung zu " + brokerAddress + " fehlgeschlagen");
            Notification.show("Verbindung zum MQTT-Broker fehlgeschlagen");
        }
    }

    private void disconnectFromBroker() {
        mqttConnectionService.disconnect();
        showDisconnected("Nicht verbunden");
    }

    private void publishMessage(String topic, String message) {
        if (!mqttConnectionService.isConnected()) {
            Notification.show("Bitte zuerst eine Verbindung zum MQTT-Broker herstellen");
            return;
        }

        mqttConnectionService.publish(topic, message);
    }

    private void refreshGrid() {
        if (grid == null) {
            return;
        }

        if (getUI().isPresent()) {
            getUI().get().access(() -> grid.setItems(mqttValueService.findAll()));
        } else {
            grid.setItems(mqttValueService.findAll());
        }
    }

    private void refreshConnectionStatus() {
        if (mqttConnectionService.isConnected()) {
            showConnected(mqttConnectionService.getCurrentBrokerAddress());
        } else {
            showDisconnected("Nicht verbunden");
        }
    }

    private void showConnected(String brokerAddress) {
        setConnectionStatus("Verbunden mit " + brokerAddress, true);
    }

    private void showDisconnected(String message) {
        setConnectionStatus(message, false);
    }

    private void showConnectionLost() {
        showDisconnected("Verbindung verloren");
    }

    private void setConnectionStatus(String message, boolean connected) {
        Runnable update = () -> {
            connectionStatus.setText(message);
            connectionStatus.getStyle().set(
                    "color",
                    connected ? "var(--lumo-success-text-color)" : "var(--lumo-error-text-color)");
        };

        if (getUI().isPresent()) {
            getUI().get().access(update::run);
        } else {
            update.run();
        }
    }
}
