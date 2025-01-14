package com.example.hashtrack.consumer;

import com.example.hashtrack.collection.StreamItem;
import com.example.hashtrack.collection.StreamItemDocument;
import com.example.hashtrack.repository.StreamItemRepository;
import com.example.hashtrack.request.SourceData;
import com.example.hashtrack.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SourceDataConsumer {

    private final StreamItemRepository streamItemRepository;
    private final CacheService cacheService;


    @KafkaListener(topics = "StreamData", groupId = "mongo-consumer-group")
    public void consume(ConsumerRecord<String, SourceData> record) {
        log.info("Consuming message. topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());

        resetTraceId(record);
        String sourceId = record.key();
        SourceData message = record.value();
        int lastTwoDigits = Integer.parseInt(message.getLastTwoCharsOfHash(), 16);
        if (lastTwoDigits > 99) {
            long previousOffset = record.offset() - 1;
            String prevDocKey = docIdKey(sourceId, previousOffset);
            String existingDocId = cacheService.get(prevDocKey);
            log.debug("lastTwoDigits={} > 99. Checking Redis for docKey={}, foundedDocId={}", lastTwoDigits, prevDocKey, existingDocId);
            if (existingDocId != null) {
                updateExistingDocument(record, existingDocId, message);
            } else {
                StreamItemDocument newDoc = createNewDocument(message, record.offset());
                saveCurrentOffsetKeyInCache(record, newDoc.getId());
                log.info("Created new document with ID={} because no existing docId found in Redis", newDoc.getId());
            }
        } else {
            StreamItemDocument newDoc = createNewDocument(message, record.offset());
            log.info("Created new document with ID={} for lastTwoDigits={}", newDoc.getId(), lastTwoDigits);
        }
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

    private void updateExistingDocument(ConsumerRecord<String, SourceData> record, String existingDocId, SourceData message) {
        Optional<StreamItemDocument> optionalDoc = streamItemRepository.findById(existingDocId);
        if (optionalDoc.isPresent()) {
            StreamItemDocument existingDoc = optionalDoc.get();
            existingDoc.setSourceId(record.key());
            existingDoc.getSourceDataList().add(mapToSourceItem(message, record.offset()));
            streamItemRepository.save(existingDoc);
            log.info("Appended new SourceData to existing docId={} in MongoDB", existingDocId);
            saveCurrentOffsetKeyInCache(record, existingDocId);
        } else {
            log.error("Document Id could not be found in MongoDB {}", existingDocId);
        }
    }

    private void saveCurrentOffsetKeyInCache(ConsumerRecord<String, SourceData> record, String newDoc) {
        String currentOffsetKey = docIdKey(record.key(), record.offset());
        cacheService.set(currentOffsetKey, newDoc);
    }

    private StreamItemDocument createNewDocument(SourceData message, long offset) {
        StreamItemDocument newDoc = new StreamItemDocument();
        newDoc.setId(UUID.randomUUID().toString());
        newDoc.getSourceDataList().add(mapToSourceItem(message, offset));
        return streamItemRepository.save(newDoc);
    }

    private StreamItem mapToSourceItem(SourceData message, long offset) {
        StreamItem streamItem = new StreamItem();
        streamItem.setTimestamp(message.getTimestamp());
        streamItem.setRandomValue(message.getRandomValue());
        streamItem.setOffset(offset);
        streamItem.setLastTwoCharsOfHash(message.getLastTwoCharsOfHash());
        return streamItem;
    }

    private String docIdKey(String sourceId, long offset) {
        return String.format("docId:%s:%d", sourceId, offset);
    }
}

