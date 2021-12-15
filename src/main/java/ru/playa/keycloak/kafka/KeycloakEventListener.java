package ru.playa.keycloak.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.admin.AdminEvent;

/**
 * Sends Keycloak events to Kafka.
 *
 * @author Aleksandr Buldaev
 * @author Anatoliy Pokhresnyi
 */
public class KeycloakEventListener implements EventListenerProvider {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(KeycloakEventListener.class);

    /**
     * Configuration.
     */
    private final Configuration configuration;

    /**
     * Producer.
     */
    private volatile KafkaProducer<String, byte[]> eventProducer;

    /**
     * Get configuration during initialization.
     */
    public KeycloakEventListener() {
        configuration = new Configuration();
    }

    @Override
    public void onEvent(Event event) {
        LOGGER.infof("Send event %s", event.toString());
        publish(
                configuration.getLoginEventTopic(),
                getKafkaEventProducer(),
                KeycloakEvent.of(event)
        );
    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean includeRepresentation) {
        LOGGER.infof("Send event %s", adminEvent.toString());
        publish(
                configuration.getAdminEventTopic(),
                getKafkaEventProducer(),
                KeycloakAdminEvent.of(adminEvent)
        );
    }

    @Override
    public void close() {
        if (eventProducer != null) {
            try {
                eventProducer.close();
            } catch (Exception e) {
                LOGGER.warn("Can't close admin event producer", e);
            }
        }
    }

    /**
     * Sending a message to kafka.
     *
     * @param topic    Topic.
     * @param producer Producer.
     * @param event    Event
     * @param <T>      Type of event.
     */
    private <T> void publish(String topic, KafkaProducer<String, byte[]> producer, T event) {
        try {
            byte[] message = new ObjectMapper().writeValueAsString(event).getBytes();
            ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, message);

            if (configuration.isSync()) {
                producer.send(record).get();
                producer.flush();
            } else {
                producer.send(record);
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            /*
             * We will try to re-init producer on next call
             */
            eventProducer = null;
            if (configuration.isThrowExceptionOnError()) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    /**
     * Getting producer from configuration on first send.
     *
     * @return Producer.
     */
    private KafkaProducer<String, byte[]> getKafkaEventProducer() {
        if (eventProducer == null) {
            synchronized (this) {
                // Workaround for SCRAM auth issue
                // javax.security.auth.login.LoginException:
                // No LoginModule found for org.apache.kafka.common.security.scram.ScramLoginModule
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                eventProducer = new KafkaProducer<>(
                        configuration.getKafkaConfiguration(), new StringSerializer(), new ByteArraySerializer()
                );
            }
        }
        return eventProducer;
    }
}
