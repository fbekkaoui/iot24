package de.feswiesbaden.iot.mqttClient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MqttValueRepository extends JpaRepository<MqttValue, Long>{
    
}