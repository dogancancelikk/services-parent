package com.example.hashtrack.repository;

import com.example.hashtrack.collection.StreamItemDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamItemRepository extends MongoRepository<StreamItemDocument, String> {
}
