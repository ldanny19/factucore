# FactuCore Messaging

Librería técnica genérica para publicación y consumo de eventos mediante Kafka o RabbitMQ.

## Responsabilidad

La librería únicamente resuelve infraestructura de mensajería:

- API genérica de publicación y consumo.
- Múltiples conexiones de mensajería dentro del mismo microservicio.
- Múltiples destinos lógicos.
- Selección de Kafka o RabbitMQ por conexión.
- Reutilización de clientes por conexión.
- Reintentos.
- Dead Letter Queue/Topic.
- Serialización del contrato `EventoMensaje`.
- Auto-configuración Spring Boot.

No contiene lógica de negocio ni conoce Facturador, Notification, Auditoría, ERP o dominios funcionales.

## Configuración

Se separan dos conceptos:

- **conexión**: define cómo conectarse al broker.
- **destino**: define qué conexión utilizar y cuál es el tópico/cola físico.

Un evento se publica a **un único destino lógico por llamada**. La librería no hace fan-out automático por tipo de evento.

Kafka:

```yaml
factucore:
  messaging:
    enabled: true

    conexiones:
      kafka-principal:
        tipo: KAFKA
        kafka:
          bootstrap-servers: localhost:9092

    destinos:
      auditoria:
        conexion: kafka-principal
        nombre: auditoria

      notificacion:
        conexion: kafka-principal
        nombre: notificacion
```

En este ejemplo ambos destinos utilizan el mismo servidor Kafka, pero son tópicos diferentes:

```text
Kafka localhost:9092
├── auditoria
└── notificacion
```

También pueden utilizar conexiones diferentes:

```yaml
factucore:
  messaging:
    conexiones:
      kafka-auditoria:
        tipo: KAFKA
        kafka:
          bootstrap-servers: kafka-auditoria:9092

      kafka-notificacion:
        tipo: KAFKA
        kafka:
          bootstrap-servers: kafka-notificacion:9092

    destinos:
      auditoria:
        conexion: kafka-auditoria
        nombre: auditoria

      notificacion:
        conexion: kafka-notificacion
        nombre: notificacion
```

RabbitMQ utiliza el mismo modelo:

```yaml
factucore:
  messaging:
    conexiones:
      rabbit-principal:
        tipo: RABBITMQ
        rabbitmq:
          addresses: localhost:5672
          username: guest
          password: guest
          virtual-host: /

    destinos:
      auditoria:
        conexion: rabbit-principal
        nombre: auditoria
```

## Publicar

El microservicio selecciona explícitamente el destino lógico:

```java
publicadorMensajes.publicar("auditoria", evento);
```

o:

```java
publicadorMensajes.publicar("notificacion", evento);
```

El microservicio no conoce Kafka, RabbitMQ ni la implementación concreta del productor.

## Consumir

Un microservicio implementa `ConsumidorMensajes` indicando el destino lógico:

```java
@Component
public class MiConsumidor implements ConsumidorMensajes {

    @Override
    public String destino() {
        return "auditoria";
    }

    @Override
    public String grupo() {
        return "factucore-auditoria";
    }

    @Override
    public void consumir(EventoMensaje evento) {
        // lógica del microservicio
    }
}
```

La librería resuelve automáticamente la conexión y el broker asociados al destino.

## Resiliencia

Los consumidores tienen reintentos configurables y DLQ/DLT cuando están habilitados:

```yaml
factucore:
  messaging:
    retry:
      intervalo-ms: 5000
      max-intentos: 3
      dlq-habilitada: true
      sufijo-dlq: .DLQ
```
