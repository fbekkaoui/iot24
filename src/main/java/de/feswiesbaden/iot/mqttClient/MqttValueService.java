package de.feswiesbaden.iot.mqttClient;



import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MqttValueService {
  
    private MqttValueRepository repository;
    
    public MqttValueService(@Autowired MqttValueRepository repository) {
        this.repository = repository;
    }

    public Optional<MqttValue> get(UUID id) {
        return repository.findById(id);
    }

    public MqttValue update(MqttValue entity) {
        return repository.save(entity);
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }

    public List<MqttValue> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }


}
