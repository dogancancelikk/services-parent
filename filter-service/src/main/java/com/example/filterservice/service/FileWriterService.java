package com.example.filterservice.service;

import com.example.filterservice.message.SourceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileWriterService {

    private static final String FILE_PATH = "output.txt";

    public FileWriterService() {
        try {
            if (!Files.exists(Paths.get(FILE_PATH))) {
                Files.createFile(Paths.get(FILE_PATH));
            }
        } catch (IOException e) {
            log.error("Failed to initialize output file: {}", FILE_PATH, e);
        }
    }

    public synchronized void appendData(SourceData data) {
        String line = String.format("%s,%d,%s%n",
                data.getTimestamp(), data.getRandomValue(), data.getLastTwoCharsOfHash());

        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            writer.write(line);
            log.info("Successfully wrote data to file: {}", line.trim());
        } catch (IOException e) {
            log.error("Error writing data to file: {}", line.trim(), e);
        }
    }
}
