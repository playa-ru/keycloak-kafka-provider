package ru.playa.keycloak.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlRootElement;
import org.keycloak.events.admin.AdminEvent;

/**
 * Object copy of @{code org.keycloak.events.admin.AdminEvent}.
 * A copy of the object is required to perform serialization and deserialization of the object in Json.
 *
 * @author Anatoliy Pokhresnyi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@XmlRootElement
public class KeycloakAdminEvent extends AdminEvent implements Serializable {

    private static final long serialVersionUID = -7367949289101799624L;

    /**
     * Преобразование @{code org.keycloak.events.admin.AdminEvent} в @{code ru.playa.keycloak.kafka.KeycloakAdminEvent}.
     *
     * @param adminEvent Событие из Keycloak.
     * @return Преобразованное событие.
     */
    public static KeycloakAdminEvent of(AdminEvent adminEvent) {
        KeycloakAdminEvent msg = new KeycloakAdminEvent();
        msg.setAuthDetails(adminEvent.getAuthDetails());
        msg.setError(adminEvent.getError());
        msg.setOperationType(adminEvent.getOperationType());
        msg.setRealmId(adminEvent.getRealmId());
        msg.setRepresentation(adminEvent.getRepresentation());
        msg.setResourcePath(adminEvent.getResourcePath());
        msg.setResourceType(adminEvent.getResourceType());
        msg.setTime(adminEvent.getTime());

        return msg;
    }

}
