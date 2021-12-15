package ru.playa.keycloak.kafka;

import org.junit.Assert;
import org.junit.Test;

public class ConfigurationTest {

    @Test
    public void testCustomConfiguration() {

        System.setProperty("jboss.server.config.dir", "src/test/resources");

        Configuration configuration = new Configuration();
        Assert.assertEquals("keycloak-admin-events-custom", configuration.getAdminEventTopic());
        Assert.assertEquals("keycloak-login-events-custom", configuration.getLoginEventTopic());
        Assert.assertTrue(configuration.isSync());
        Assert.assertTrue(configuration.isThrowExceptionOnError());
        Assert.assertEquals("custom:9092", configuration.getKafkaConfiguration().get("bootstrap.servers"));
        Assert.assertEquals("-1", configuration.getKafkaConfiguration().get("acks"));
        Assert.assertEquals("1000", configuration.getKafkaConfiguration().get("max.block.ms"));
        Assert.assertEquals("custom-value", configuration.getKafkaConfiguration().get("custom-property"));


    }

    @Test
    public void testConfigurationFromSystemProperty(){

        System.setProperty("jboss.server.config.dir", "src/test/resources");

        System.setProperty("kafka.max.block.ms", "3000");
        System.setProperty("kafka.custom-property", "custom-value-from-property");

        Configuration configuration = new Configuration();
        Assert.assertEquals("keycloak-admin-events-custom", configuration.getAdminEventTopic());
        Assert.assertEquals("keycloak-login-events-custom", configuration.getLoginEventTopic());
        Assert.assertTrue(configuration.isSync());
        Assert.assertTrue(configuration.isThrowExceptionOnError());
        Assert.assertEquals("custom:9092", configuration.getKafkaConfiguration().get("bootstrap.servers"));
        Assert.assertEquals("-1", configuration.getKafkaConfiguration().get("acks"));
        Assert.assertEquals("3000", configuration.getKafkaConfiguration().get("max.block.ms"));
        Assert.assertEquals("custom-value-from-property", configuration.getKafkaConfiguration().get("custom-property"));

    }

    @Test
    public void testDefaultConfiguration() {
        Configuration configuration = new Configuration();
        Assert.assertEquals("keycloak-admin-events", configuration.getAdminEventTopic());
        Assert.assertEquals("keycloak-login-events", configuration.getLoginEventTopic());
        Assert.assertFalse(configuration.isSync());
        Assert.assertFalse(configuration.isThrowExceptionOnError());
        Assert.assertEquals("localhost:9092", configuration.getKafkaConfiguration().get("bootstrap.servers"));
        Assert.assertEquals("1", configuration.getKafkaConfiguration().get("acks"));
        Assert.assertEquals("2000", configuration.getKafkaConfiguration().get("max.block.ms"));
    }

}
