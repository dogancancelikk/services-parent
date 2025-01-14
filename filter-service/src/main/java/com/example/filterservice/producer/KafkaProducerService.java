package com.example.filterservice.producer;

import com.example.filterservice.message.SourceData;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, SourceData> kafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String topicName;

    public void send(SourceData data) {
        var message = MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.TOPIC, topicName)
                .setHeader(KafkaHeaders.KEY, MDC.get("connectionId"))
                .setHeader("traceId", MDC.get("traceId"))
                .build();

        kafkaTemplate.send(message);
    }
}
