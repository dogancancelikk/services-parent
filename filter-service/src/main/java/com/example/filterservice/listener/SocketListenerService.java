package com.example.filterservice.listener;

import com.example.filterservice.message.SourceData;
import com.example.filterservice.producer.KafkaProducerService;
import com.example.filterservice.service.FileWriterService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketListenerService {

    @Value("${socket.listener.port:5027}")
    private int port;

    private final KafkaProducerService kafkaProducerService;
    private final FileWriterService fileWriterService;
    private final ExecutorService clientHandlerPool = Executors.newCachedThreadPool();

    @PostConstruct
    public void startListener() {
        Thread listenerThread = new Thread(this::startSocketServer, "SocketListenerThread");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void startSocketServer() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            log.info("SocketListenerService started on port: {}", port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                clientHandlerPool.submit(() -> handleClient(clientSocket));
            }
        } catch (Exception e) {
            log.error("Error starting the socket server on port {}", port, e);
        }
    }

    private void handleClient(Socket clientSocket) {
        String connectionId = UUID.randomUUID().toString();
        MDC.put("connectionId", connectionId);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                MDC.put("traceId", UUID.randomUUID().toString());
                processLine(line);
            }
        } catch (Exception e) {
            log.error("Error handling client connection", e);
        } finally {
            try {
                clientSocket.close();
            } catch (Exception e) {
                log.warn("Error closing client socket", e);
            }
        }
    }

    private void processLine(String line) {
        try {
            SourceData data = parseLine(line);
            if (data.getRandomValue() > 90) {
                kafkaProducerService.send(data);
            } else {
                log.info("data appended to the file: {}", line);
                fileWriterService.appendData(data);
            }
        } catch (Exception e) {
            log.error("Error processing line: {}", line, e);
        }
    }

    private SourceData parseLine(String line) {
        try {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String timestamp = parts[0];
                int randomValue = Integer.parseInt(parts[1]);
                String hash = parts[2];
                return new SourceData(timestamp, randomValue, hash);
            } else {
                log.warn("Invalid data format: {}", line);
            }
        } catch (Exception e) {
            log.error("Failed to parse line: {}", line, e);
        }
        return null;
    }
}
