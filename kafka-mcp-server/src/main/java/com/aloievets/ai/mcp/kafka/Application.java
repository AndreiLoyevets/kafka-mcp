package com.aloievets.ai.mcp.kafka;

import com.aloievets.ai.mcp.kafka.client.KafkaStatusViewer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final KafkaStatusViewer kafkaStatusViewer;

    public Application(final KafkaStatusViewer kafkaStatusViewer) {
        this.kafkaStatusViewer = kafkaStatusViewer;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(final String... args) throws Exception {
        System.out.println(kafkaStatusViewer.listTopics());
    }
}
