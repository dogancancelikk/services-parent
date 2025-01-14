package com.example.storageservice.listener;


import com.example.storageservice.entity.SourceDataEntity;
import com.example.storageservice.repository.SourceDataRepository;
import com.example.storageservice.request.SourceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SourceDataConsumer {

    private final SourceDataRepository sourceDataRepository;

    @KafkaListener(topics = "StreamData", groupId = "rdbms-consumer-group")
    public void consume(ConsumerRecord<String, SourceData> record) {
        resetTraceId(record);
        log.info("Consuming message. topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());
        SourceDataEntity sourceDataEntity = mapToEntity(record.value());
        log.info("sourceDataEntity={}", sourceDataEntity);
        sourceDataRepository.save(sourceDataEntity);
    }

    private SourceDataEntity mapToEntity(SourceData message) {
        SourceDataEntity sourceDataEntity = new SourceDataEntity();
        sourceDataEntity.setRandomValue(message.getRandomValue());
        sourceDataEntity.setTimestamp(message.getTimestamp());
        sourceDataEntity.setHashLastTwoChars(message.getLastTwoCharsOfHash());
        return sourceDataEntity;
    }

    private void resetTraceId(ConsumerRecord<String, SourceData> record) {
        var traceIdHeader = record.headers().lastHeader("traceId");
        if (traceIdHeader != null) {
            String traceId = new String(traceIdHeader.value(), java.nio.charset.StandardCharsets.UTF_8);
            MDC.put("traceId", traceId);
            log.debug("Extracted traceId={} from message header", traceId);
        }
        MDC.put("connectionId", record.key());
    }

}
