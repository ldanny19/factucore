# FactuCore Messaging

Librería técnica genérica para publicación y consumo de eventos mediante Kafka o RabbitMQ.

## Responsabilidad

La librería únicamente resuelve infraestructura de mensajería:

- API genérica de publicación y consumo.
- Selección del broker mediante configuración.
- Conexiones y clientes del broker.
- Registro de consumidores.
- Reintentos.
- Dead Letter Queue/Topic.
- Serialización del contrato `EventoMensaje`.
- Auto-configuración Spring Boot.

No contiene lógica de negocio ni conoce Facturador, Notification, ERP o dominios funcionales.

## Configuración

Kafka:

```yaml
factucore:
  messaging:
    broker: KAFKA
    kafka:
      bootstrap-servers: localhost:9092
```

RabbitMQ:

```yaml
factucore:
  messaging:
    broker: RABBITMQ
    rabbitmq:
      addresses: localhost:5672
      username: guest
      password: guest
      virtual-host: /
```

## Publicar

Un microservicio inyecta `PublicadorMensajes` y publica un `EventoMensaje`. No necesita conocer Kafka ni RabbitMQ.

## Consumir

Un microservicio implementa `ConsumidorMensajes`:

```java
@Component
public class MiConsumidor implements ConsumidorMensajes {

    @Override
    public String topico() {
        return "notificacion.comprobante";
    }

    @Override
    public String grupo() {
        return "notification";
    }

    @Override
    public void consumir(EventoMensaje evento) {
        // lógica del microservicio
    }
}
```

El consumidor es registrado automáticamente en el broker seleccionado.

## Resiliencia

Los consumidores tienen reintentos configurables y DLQ/DLT cuando están habilitados.

```yaml
factucore:
  messaging:
    retry:
      intervalo-ms: 5000
      max-intentos: 3
      dlq-habilitada: true
      sufijo-dlq: .DLQ
```

La librería no implementa pools de conexión artificiales comunes a ambos brokers: utiliza los mecanismos nativos de cada tecnología para reutilización, concurrencia y recuperación de conexiones.
