FROM playaru/keycloak-russian:13.0.0

ENV JBOSS_HOME /opt/jboss/keycloak

USER root

COPY target/keycloak-kafka-provider.jar $JBOSS_HOME/standalone/deployments/keycloak-kafka-provider.jar