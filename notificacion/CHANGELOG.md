# Changelog — Notification

## 1.0.0

Versión inicial del microservicio.

### Notificaciones
- Base para procesamiento de eventos de notificación.
- Integración con correo electrónico mediante Spring Mail.
- Configuración SMTP externalizada.
- Soporte para contraseña SMTP entregada desde Vault/configtree.

### Mensajería
- Integración con `factucore-messaging`.
- Kafka como broker configurado actualmente.
- Procesamiento de eventos de comprobantes autorizados.

### Observabilidad
- Integración con la infraestructura compartida de mensajería y Actuator.

### Versiones principales
- Java 21
- Spring Boot 3.5.16
- FactuCore Messaging 1.0.0
