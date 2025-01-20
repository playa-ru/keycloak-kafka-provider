package ru.playa.keycloak.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Utility class for handling Kafka and event listener configuration settings.
 *
 * @author Anatoliy Pokhresnyi
 */
public final class Configuration {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Configuration.class);

    /**
     * Listener properties prefix.
     */
    private static final String KEYCLOAK_PREFIX = "keycloak.";

    /**
     * Kafka client properties prefix.
     */
    private static final String KAFKA_PREFIX = "kafka.";

    /**
     * Topic for login events.
     */
    private static final String KEYCLOAK_KAFKA_EVENT_TOPIC = KEYCLOAK_PREFIX + KAFKA_PREFIX + "topic.login";

    /**
     * Topic for admin events.
     */
    private static final String KEYCLOAK_KAFKA_ADMIN_EVENT_TOPIC = KEYCLOAK_PREFIX + KAFKA_PREFIX + "topic.admin";

    /**
     * Use synchronous send.
     */
    private static final String KEYCLOAK_KAFKA_SYNC_MODE = KEYCLOAK_PREFIX + KAFKA_PREFIX + "sync";

    /**
     * Dry run.
     */
    private static final String KEYCLOAK_KAFKA_DRY_RUN = KEYCLOAK_PREFIX + KAFKA_PREFIX + "dryrun";

    /**
     * Throw runtime exception on send error.
     */
    private static final String KEYCLOAK_KAFKA_THROW_EX_ON_ERROR =
        KEYCLOAK_PREFIX + KAFKA_PREFIX + "throw-exception-on-error";

    /**
     * Standard Kafka property - bootstrap.servers.
     */
    private static final String KAFKA_BOOTSTRAP_SERVERS_CONFIG = KAFKA_PREFIX + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;

    /**
     * Standard Kafka property - acks.
     */
    private static final String KAFKA_ACKS = KAFKA_PREFIX + ProducerConfig.ACKS_CONFIG;

    /**
     * Standard Kafka property - max.block.ms.
     */
    private static final String KAFKA_MAX_BLOCK_MS = KAFKA_PREFIX + ProducerConfig.MAX_BLOCK_MS_CONFIG;

    /**
     * Configuration file name.
     */
    private static final String KEYCLOAK_KAFKA_CONFIG_NAME = "KEYCLOAK_KAFKA_CONFIG";

    /**
     * Default configuration file name.
     */
    private static final String KEYCLOAK_KAFKA_CONFIG_FILE = "/keycloak-kafka.properties";

    /**
     * Jboss configuration directory.
     */
    private static final String JBOSS_CONFIG_DIR = "jboss.server.config.dir";

    /**
     * Kafka provider configuration.
     */
    private final Properties kafkaConfiguration;

    /**
     * Topic for Keycloak events.
     */
    private final String adminTopic;

    /**
     * Topic for Keycloak login events.
     */
    private final String eventTopic;

    /**
     * Dry run.
     */
    private final boolean dryRun;

    /**
     * Send messages synchronously.
     */
    private final boolean sync;

    /**
     * Throw RuntimeException on send error.
     */
    private final boolean throwExceptionOnError;

    /**
     * Constructor.
     */
    public Configuration() {
        Properties configuration = loadConfiguration();
        kafkaConfiguration = loadKafkaConfiguration(configuration);

        adminTopic = configuration.getProperty(KEYCLOAK_KAFKA_ADMIN_EVENT_TOPIC, "keycloak-admin-events");
        eventTopic = configuration.getProperty(KEYCLOAK_KAFKA_EVENT_TOPIC, "keycloak-login-events");
        sync = isTrue(configuration.getProperty(KEYCLOAK_KAFKA_SYNC_MODE, "false"));
        dryRun = isTrue(configuration.getProperty(KEYCLOAK_KAFKA_DRY_RUN, "true"));
        throwExceptionOnError = isTrue(configuration.getProperty(KEYCLOAK_KAFKA_THROW_EX_ON_ERROR, "false"));
    }

    /**
     * Get configuration for kafka client.
     *
     * @return kafka client configuration
     */
    public Properties getKafkaConfiguration() {
        return kafkaConfiguration;
    }

    /**
     * Getting a topic for admin events.
     *
     * @return Topic.
     */
    public String getAdminEventTopic() {
        return adminTopic;
    }

    /**
     * Getting a topic for login events.
     *
     * @return Topic.
     */
    public String getLoginEventTopic() {
        return eventTopic;
    }

    /**
     * Use synchronous approach for send.
     *
     * @return Acknowledge.
     */
    public boolean isSync() {
        return sync;
    }

    /**
     * Throw exception if send failed.
     *
     * @return Is failed then throw.
     */
    public boolean isThrowExceptionOnError() {
        return throwExceptionOnError;
    }

    /**
     * Is dru run.
     *
     * @return Dry run.
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * Set variable if absent.
     *
     * @param properties   Properties.
     * @param key          Key.
     * @param defaultValue Default value.
     */
    private static void ifAbsentThenSet(Properties properties, String key, String defaultValue) {
        if (isEmpty(properties.getProperty(key))) {
            properties.setProperty(key, defaultValue);
        }
    }

    /**
     * Check for emptiness.
     *
     * @param value Value.
     * @return String is empty.
     */
    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Check for true.
     *
     * @param value Value.
     * @return Check result.
     */
    private static boolean isTrue(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on") || value.equalsIgnoreCase("1");
    }

    /**
     * Getting a variable from environment variables.
     *
     * @param key          Key.
     * @param defaultValue Default value.
     * @return Value.
     */
    private static String getenv(String key, String defaultValue) {
        String env = System.getenv(key);
        String property = System.getProperty(key);

        if (env == null && property == null) {
            return defaultValue;
        }

        return env == null ? property : env;
    }

    /**
     * Loading default configuration.
     *
     * @return Default configuration.
     */
    private Properties loadConfiguration() {
        Properties properties = new Properties();

        String filename = getenv(
            KEYCLOAK_KAFKA_CONFIG_NAME,
            getenv(JBOSS_CONFIG_DIR, "") + KEYCLOAK_KAFKA_CONFIG_FILE
        );

        if (new File(filename).exists()) {
            try (FileInputStream inStream = new FileInputStream(filename)) {
                properties.load(inStream);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        ifAbsentThenSet(properties, KAFKA_BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        ifAbsentThenSet(properties, KAFKA_ACKS, "1");
        ifAbsentThenSet(properties, KAFKA_MAX_BLOCK_MS, "2000");

        for (Object o : properties.keySet()) {
            String key = (String) o;
            String envVariableName = key.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
            LOGGER.debugf("Check for environment variable: %s", envVariableName);
            String envVariableValue = System.getenv(envVariableName);
            if (envVariableValue != null) {
                LOGGER.debugf("Set value from environment variable: %s", envVariableValue);
                properties.setProperty(key, envVariableValue);
            } else if (System.getProperty(key) != null) {
                String property = System.getProperty(key);
                LOGGER.debugf("Set value from system property: %s", property);
                properties.setProperty(key, property);
            }
        }

        LOGGER.infof("Kafka final configuration: %s", properties);

        return properties;
    }

    /**
     * Loading Kafka configuration.
     *
     * @param properties Properties.
     * @return Kafka configuration.
     */
    private Properties loadKafkaConfiguration(Properties properties) {
        Properties kafka = new Properties();

        properties
            .stringPropertyNames()
            .stream()
            .filter(name -> !name.startsWith(KEYCLOAK_PREFIX))
            .filter(name -> !(KAFKA_PREFIX + ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG).equals(name))
            .filter(name -> !ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG.equals(name))
            .filter(name -> !(KAFKA_PREFIX + ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG).equals(name))
            .filter(name -> !ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG.equals(name))
            .forEach(name -> {
                String newName = name.startsWith(KAFKA_PREFIX) ? name.replaceFirst(KAFKA_PREFIX, "") : name;

                kafka.setProperty(newName, properties.getProperty(name));
            });

        LOGGER.debugf("Loading Kafka configuration %s", kafka);

        return kafka;
    }
}
