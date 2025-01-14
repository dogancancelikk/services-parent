package com.example.hashtrack.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "source_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamItemDocument {
    @Id
    private String id;
    private String sourceId;
    private List<StreamItem> sourceDataList = new ArrayList<>();
}
