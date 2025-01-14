package com.example.hashtrack.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceData implements Serializable {
    private String timestamp;
    private Integer randomValue;
    private String lastTwoCharsOfHash;
}
