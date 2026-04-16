package de.feswiesbaden.iot.data.mqttclient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class MqttValueService {

    private final MqttValueRepository repository;

    public MqttValueService(MqttValueRepository repository) {
        this.repository = repository;
    }

    public MqttValue save(MqttValue entity) {
        return repository.save(entity);
    }

    public List<MqttValue> findAll() {
        return repository.findAll();
    }

    public long countAll() {
        return repository.count();
    }

    public int countTopics() {
        return countByTopic().size();
    }

    public MqttValue findLatest() {
        return repository.findTopByOrderByTimeStampDesc();
    }

    public List<MqttValue> findRecent(int limit) {
        List<MqttValue> values = limit <= 12
                ? repository.findTop12ByOrderByTimeStampDesc()
                : repository.findTop24ByOrderByTimeStampDesc();
        if (values.size() <= limit) {
            return values;
        }
        return values.subList(0, limit);
    }

    public Map<String, Long> countByTopic() {
        return repository.findAll().stream()
                .collect(Collectors.groupingBy(MqttValue::getTopic, LinkedHashMap::new, Collectors.counting()));
    }
}
