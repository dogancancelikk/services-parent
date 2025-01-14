package com.example.filterservice.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceData implements Serializable {
    private String timestamp;
    private int randomValue;
    private String lastTwoCharsOfHash;
}

