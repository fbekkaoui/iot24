package de.feswiesbaden.iot.mqttclient;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class MqttValueService {

    private final MqttValueRepository repository;

    public MqttValueService(MqttValueRepository repository) {
        this.repository = repository;
    }

    public Optional<MqttValue> get(Long id) {
        return repository.findById(id);
    }

    public MqttValue update(MqttValue entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<MqttValue> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<MqttValue> list(Pageable pageable, Specification<MqttValue> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
