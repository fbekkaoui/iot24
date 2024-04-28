package de.feswiesbaden.iot.data.mqttclient;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MqttValueRepository extends JpaRepository<MqttValue, Long>, JpaSpecificationExecutor<MqttValue> {

}
