package de.feswiesbaden.iot.data.mqttclient;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MqttValueRepository extends JpaRepository<MqttValue, Long> {
    MqttValue findTopByOrderByTimeStampDesc();
    List<MqttValue> findTop12ByOrderByTimeStampDesc();
    List<MqttValue> findTop24ByOrderByTimeStampDesc();
}
