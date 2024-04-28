package de.feswiesbaden.iot.views;



import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;

import de.feswiesbaden.iot.data.mqttclient.MqttValue;


public class MainViewController {
    private UI ui;
    private Grid<MqttValue> grid;

    public MainViewController(UI ui, Grid<MqttValue> grid){

        this.ui=ui;
        this.grid=grid;
    }

    public void updateGrid(){
        
        ui.access(() -> grid.getDataProvider().refreshAll());
    }

    public void setParam(UI ui, Grid<MqttValue> grid) {

        this.ui=ui;
        this.grid=grid;
    }
    
}
