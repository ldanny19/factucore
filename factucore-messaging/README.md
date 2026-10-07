# FactuCore Messaging

Librería técnica genérica para publicación y consumo de eventos mediante Kafka o RabbitMQ.

## Responsabilidad

La librería únicamente resuelve infraestructura de mensajería:

- API genérica de publicación y consumo.
- Selección del broker mediante configuración.
- Conexiones y clientes del broker.
- Enrutamiento por tipo de evento.
- Registro de múltiples consumidores.
- Reintentos.
- Dead Letter Queue/Topic.
- Serialización del contrato `EventoMensaje`.
- Auto-configuración Spring Boot.

No contiene lógica de negocio ni conoce Facturador, Notification, ERP o dominios funcionales.

## Configuración

El productor no recibe el tópico desde cada microservicio. El destino se resuelve centralizadamente mediante el tipo de evento. Un tipo puede tener uno o varios destinos.

Kafka:

```yaml
factucore:
  messaging:
    broker: KAFKA
    kafka:
      bootstrap-servers: localhost:9092
    rutas:
      REGISTRO_AUDITORIA_HTTP:
        - factucore.auditoria.peticion
      COMPROBANTE_AUTORIZADO:
        - factucore.notificacion.comprobante
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
    rutas:
      REGISTRO_AUDITORIA_HTTP:
        - factucore.auditoria.peticion
```

La misma definición de rutas funciona para ambos brokers. El broker seleccionado se encarga de traducir el destino lógico a tópico/exchange/routing key según su implementación.

## Publicar

Un microservicio inyecta `PublicadorMensajes`, crea un `EventoMensaje` y publica el evento sin conocer Kafka, RabbitMQ ni el destino físico:

```java
publicadorMensajes.publicar(evento);
```

La librería obtiene `evento.tipo()`, busca sus destinos configurados y publica en todos ellos.

Si un tipo de evento no tiene una ruta válida, la publicación falla para evitar pérdida silenciosa de mensajes.

## Consumir

Un microservicio implementa `ConsumidorMensajes`:

```java
@Component
public class MiConsumidor implements ConsumidorMensajes {

    @Override
    public String topico() {
        return "factucore.notificacion.comprobante";
    }

    @Override
    public String grupo() {
        return "notification";
    }

    @Override
    public Set<String> tiposEvento() {
        return Set.of("COMPROBANTE_AUTORIZADO");
    }

    @Override
    public void consumir(EventoMensaje evento) {
        // lógica del microservicio
    }
}
```

El consumidor se registra automáticamente en el broker seleccionado. Se pueden registrar N consumidores, cada uno con su tópico/origen, grupo y tipos de evento.

Si `tiposEvento()` devuelve un conjunto vacío, el consumidor acepta cualquier tipo de evento del origen configurado.

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
