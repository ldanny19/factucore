# Changelog — FactuCore Messaging

## 1.0.0

Versión inicial de la librería técnica de mensajería.

### Mensajería
- API genérica para publicación y consumo.
- Soporte para Kafka.
- Soporte para RabbitMQ.
- Selección del broker mediante configuración.
- Serialización de eventos.
- Consumidores configurables.
- Reintentos.
- DLQ/DLT cuando corresponda.
- Auto-configuración Spring Boot.

### Observabilidad
- Health check dinámico según broker seleccionado.
- Exclusión de auto-configuración del broker no utilizado.

### Arquitectura
- Sin lógica de negocio de Facturador o Notification.
- Sin conocimiento de ERP/POS.
- API reusable para futuros microservicios.

### Versiones principales
- Java 21
- Spring Boot 3.5.16
- FactuCore Messaging 1.0.0
