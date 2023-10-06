package ru.playa.keycloak.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.keycloak.events.Event;

/**
 * Object copy of @{code org.keycloak.events.Event}.
 * A copy of the object is required to perform serialization and deserialization of the object in Json.
 *
 * @author Anatoliy Pokhresnyi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@XmlRootElement
public class KeycloakEvent extends Event implements Serializable {

    private static final long serialVersionUID = -2192461924304841222L;

    /**
     * Преобразование @{code org.keycloak.events.Event} в @{code ru.playa.keycloak.kafka.KeycloakEvent}.
     *
     * @param event Событие из Keycloak.
     * @return Преобразованное событие.
     */
    public static KeycloakEvent of(Event event) {
        KeycloakEvent msg = new KeycloakEvent();
        msg.setClientId(event.getClientId());
        msg.setDetails(event.getDetails());
        msg.setError(event.getError());
        msg.setIpAddress(event.getIpAddress());
        msg.setRealmId(event.getRealmId());
        msg.setSessionId(event.getSessionId());
        msg.setTime(event.getTime());
        msg.setType(event.getType());
        msg.setUserId(event.getUserId());

        return msg;
    }
}
