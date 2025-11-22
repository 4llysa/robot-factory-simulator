package fr.tp.inf112.projects.robotsim.app;

import java.util.Properties;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.model.Factory;
import org.apache.kafka.clients.consumer.ConsumerConfig;

public class SimulationServiceUtils {
    public static final String BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String GROUP_ID = "Factory-Simulation-Group";
    private static final String AUTO_OFFSET_RESET = "earliest";
    private static final String TOPIC_PREFIX = "simulation-topic-";
    public static String getTopicName(final Canvas factoryModel) {
        System.out.println(factoryModel.getId());
        return TOPIC_PREFIX + sanitizeTopic(factoryModel.getId());
    }

    private static String sanitizeTopic(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
    public static Properties getDefaultConsumerProperties() {
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        return props;
    }
}