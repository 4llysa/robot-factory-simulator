package fr.tp.inf112.projects.robotsim.app;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Logger;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.tools.javac.Main;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.app.RemoteSimulatorController;
import fr.tp.inf112.projects.robotsim.model.Factory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;

class FactorySimulationEventConsumer {
    protected transient Logger LOGGER = Logger.getLogger(Main.class.getName());

    private final KafkaConsumer<String, String> consumer;
    private final RemoteSimulatorController controller;

    public FactorySimulationEventConsumer(final RemoteSimulatorController controller) {
        this.controller = controller;
        final Properties props = SimulationServiceUtils.getDefaultConsumerProperties();
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        this.consumer = new KafkaConsumer<>(props);
        final String topicName = SimulationServiceUtils.getTopicName(controller.getCanvas());
        this.consumer.subscribe(Collections.singletonList(topicName));
    }

    public void consumeMessages() {
        try {
            while (controller.isAnimationRunning()) {
                final ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(100));
                for (final ConsumerRecord<String, String> record : records) {
                    LOGGER.fine("Received JSON Factory text '" + record.value() + "'.");
                    controller.setCanvas(record.value());
                }
            }
        } catch (WakeupException e) {
            LOGGER.info("Kafka consumer thread interrupted, stopping gracefully.");
        } finally {
            consumer.close();
        }
    }

    public void terminate() {
        consumer.wakeup();
    }
}

