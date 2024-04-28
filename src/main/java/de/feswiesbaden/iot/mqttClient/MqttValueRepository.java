package de.feswiesbaden.iot.mqttClient;



import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MqttValueRepository extends JpaRepository<MqttValue, UUID>{
    
}