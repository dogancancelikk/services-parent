package com.example.datasource.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RandomDataGenerator {

    @Value("${socket.server.host}")
    private String serverHost;

    @Value("${socket.server.port}")
    private int serverPort;

    private static final int RECORDS_PER_SECOND = 5;

    private final Random random = new Random();
    private PrintWriter writer;

    @PostConstruct
    public void startGeneratingData() {
        connectToServer();
        startScheduler();
    }

    private void connectToServer() {
        while (true) {
            try {
                Socket socket = new Socket(serverHost, serverPort);
                writer = new PrintWriter(socket.getOutputStream(), true);
                log.info("Connected to server at {}:{}", serverHost, serverPort);
                break;
            } catch (Exception e) {
                log.error("Failed to connect to server. Retrying in 3 seconds...", e);
                sleep(3000);
            }
        }
    }

    private void startScheduler() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendRandomDataBatch, 0, 1, TimeUnit.SECONDS);
    }

    private void sendRandomDataBatch() {
        try {
            for (int i = 0; i < RECORDS_PER_SECOND; i++) {
                String data = generateRandomData();
                writer.println(data);
                log.debug("Sent data: {}", data);
            }
        } catch (Exception e) {
            log.error("Error while sending data. Attempting to reconnect...", e);
            reconnectToServer();
        }
    }

    private String generateRandomData() {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        int randomValue = random.nextInt(101);
        String md5LastTwo = calculateMd5LastTwo(timestamp + randomValue);
        return String.format("%s,%d,%s", timestamp, randomValue, md5LastTwo);
    }

    private String calculateMd5LastTwo(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(sb.length() - 2);
        } catch (Exception e) {
            log.error("Failed to calculate MD5 hash for input: {}", input, e);
            return "00";
        }
    }


    private void reconnectToServer() {
        closeWriter();
        connectToServer();
    }

    private void closeWriter() {
        if (writer != null) {
            writer.close();
        }
    }

    private void sleep(int durationMillis) {
        try {
            Thread.sleep(durationMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Sleep interrupted", e);
        }
    }
}
