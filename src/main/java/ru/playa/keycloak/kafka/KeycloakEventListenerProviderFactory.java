package ru.playa.keycloak.kafka;

import org.keycloak.Config;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventListenerProviderFactory;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

/**
 * The event dispatch factory in Kafka.
 *
 * @author Aleksandr Buldaev
 * @author Anatoliy Pokhresnyi
 */
public class KeycloakEventListenerProviderFactory implements EventListenerProviderFactory {

    @Override
    public EventListenerProvider create(KeycloakSession keycloakSession) {
        return new KeycloakEventListener();
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {
    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "playa-kafka-event-listener";
    }
}
