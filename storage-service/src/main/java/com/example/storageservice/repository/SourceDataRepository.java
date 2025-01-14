package com.example.storageservice.repository;


import com.example.storageservice.entity.SourceDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceDataRepository extends JpaRepository<SourceDataEntity, Long> {
}
