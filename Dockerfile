FROM bellsoft/liberica-openjdk-centos:17 AS ubi-micro-install

ARG JAR_FILE

ARG TMP_DIST=/tmp/keycloak

ENV KEYCLOAK_VERSION 21.1.1

ENV MAVEN_CENTRAL_URL https://repo1.maven.org/maven2

ARG KEYCLOAK_DIST=https://github.com/keycloak/keycloak/releases/download/$KEYCLOAK_VERSION/keycloak-$KEYCLOAK_VERSION.tar.gz

RUN yum install -y curl tar gzip unzip

ADD $KEYCLOAK_DIST $TMP_DIST/

COPY target/$JAR_FILE $TMP_DIST/keycloak-kafka-provider-$KEYCLOAK_VERSION.jar

RUN cd /tmp/keycloak && tar -xvf /tmp/keycloak/keycloak-*.tar.gz && rm /tmp/keycloak/keycloak-*.tar.gz

RUN mv $TMP_DIST/keycloak-kafka-provider-$KEYCLOAK_VERSION.jar $TMP_DIST/keycloak-$KEYCLOAK_VERSION/providers/keycloak-kafka-provider-$KEYCLOAK_VERSION.jar

RUN mkdir -p /opt/keycloak && mv /tmp/keycloak/keycloak-$KEYCLOAK_VERSION/* /opt/keycloak && mkdir -p /opt/keycloak/data

RUN chmod -R g+rwX /opt/keycloak

FROM bellsoft/liberica-openjdk-centos:17 AS ubi-micro-chown
ENV LANG en_US.UTF-8

COPY --from=ubi-micro-install --chown=1000:0 /opt/keycloak /opt/keycloak

RUN echo "keycloak:x:0:root" >> /etc/group && \
    echo "keycloak:x:1000:0:keycloak user:/opt/keycloak:/sbin/nologin" >> /etc/passwd

USER 1000

EXPOSE 8080
EXPOSE 8443

ENTRYPOINT [ "/opt/keycloak/bin/kc.sh" ]
