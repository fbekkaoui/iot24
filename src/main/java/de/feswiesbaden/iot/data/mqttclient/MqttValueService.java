package de.feswiesbaden.iot.data.mqttclient;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.vaadin.flow.component.grid.Grid;

@Service
public class MqttValueService {

    private final MqttValueRepository repository;
    public Grid mqttGrid;

    public MqttValueService(@Autowired MqttValueRepository repository) {
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

    public List<MqttValue> findAll() {
        return repository.findAll();
    }


}
