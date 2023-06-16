package com.example.veridion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaConsumer {

    @Value("${HOSTNAME}")
    private String hostname;

    @KafkaListener(id = "${HOSTNAME}-consumer", groupId = "${HOSTNAME}-group", topics = "${kafka.topic.name}")
    public void listen(String message) {
        log.info("Message received in veridion consumer: {}", message);
        log.info("host name is: {}", hostname);
        log.info("pod index is: {}", getPodIndex());
    }

    public int getPodIndex() {
        try {
            return Integer.parseInt(this.hostname.split("-")[2]);
        } catch (Exception ex) {
            log.warn("Exception thrown while trying to extract the pod index: {}", ex.getMessage());
            return 0;
        }
    }
}
