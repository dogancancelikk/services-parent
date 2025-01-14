package com.example.hashtrack.collection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamItem {
    private String timestamp;
    private Integer randomValue;
    private String lastTwoCharsOfHash;
    private Long offset;
}
