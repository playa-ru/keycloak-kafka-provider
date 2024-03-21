Модуль интеграции с Kafka для [Keycloak](https://www.keycloak.org/). 
Позволяет отправлять все административные и связанные со входом пользователей события Keycloak в Kafka.

## Совместимость

Библиотека провайдеров проверялась на следующих версиях Keycloak:
+ 21.1.1
+ 13.0.1

**Таблица поддерживаемых версий:**

| Версия библиотеки | Версия Keycloak | Репозиторий                                      |
|:-----------------:| :-------------: | :----------------------------------------------: |
|      24.0.1       |    24.0.1       | [Maven Central](https://mvnrepository.com)       |
|      23.0.6       |    23.0.6       | [Maven Central](https://mvnrepository.com)       |
|      22.0.3       |    22.0.3       | [Maven Central](https://mvnrepository.com)       |
|      21.1.1       |    21.1.1       | [Maven Central](https://mvnrepository.com)       |
|       1.0.6       |    13.0.1       | [Maven Central](https://mvnrepository.com)       |

## Установка Kafka провайдера в Keycloak

### Если вы используете Docker:

Соберите проект и выполните docker build - вы получите образ Keycloak с установленным модулем.
```
  mvnw clean package
```
### Если вы не используете Docker

Можно установить библиотеку Kafka провайдера в ваш Keycloak самостоятельно. Для этого нужно будет вручную выполнить шаги, описанные в [Dockerfile](Dockerfile), в целом [следуя инструкции](https://www.keycloak.org/docs/latest/server_development/index.html#registering-provider-implementations):

* Соберите проект из исходников с помощью Maven, или [возьмите готовый keycloak-kafka-provider.jar в нашем репозитории](https://repo1.maven.org/maven2/ru/playa/keycloak/keycloak-kafka-provider/).
* Скопируйте `keycloak-kafka-provider.jar` в [директорию] `${keycloak.home.dir}/standalone/deployments`.

## Настройка Kafka провайдера в Keycloak

Конфигурация Kafka клиента и параметров модуля интеграции управляется через файл настроек. По умолчанию модуль 
ищет файл `${jboss.server.config.dir}/keycloak-kafka.properties`, что обычно соответствует `/opt/jboss/keycloak/standalone/configuration/keycloak-kafka.properties`. 
Расположение файла может быть переопределено переменной окружения `KEYCLOAK_KAFKA_CONFIG`, что может быть полезно если вы используете docker/kubernetes и хотите разместить файл на примонтированном диске или в ConfigMap. 

Если конфигурационный файл не найден, устанавливаются следующие значение по умолчанию.

```
kafka.bootstrap.servers=localhost:9092
kafka.acks=1

keycloak.kafka.sync=false
keycloak.kafka.throw-exception-on-error=false
keycloak.kafka.topic.admin=keycloak-admin-events
keycloak.kafka.topic.login=keycloak-login-events
```

Вы можете задать в конфигурационном файле любые параметры продюсера, [распознаваемые Kafka клиентом](https://kafka.apache.org/documentation/#producerconfigs), 
они должны иметь префикс "kafka.", который будет удалён при передаче параметра в Kafka client.
Например, если вы хотите задать параметр client.id, то надо добавить в файл параметр kafka.client.id: 

```
kafka.client.id=my-keycloak
```

Переопределить стандартно заданные или описанные в файле конфигурации параметры можно через переменные окружения. 
Для этого необходимо заменить в названии параметра всё, кроме латинских букв и цифр на `_` и преобразовать буквы в верхний регистр. 
Например: параметр `kafka.bootstrap.servers` в переменных окружения будет именоваться `KAFKA_BOOTSTRAP_SERVERS`

