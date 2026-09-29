# Changelog — Facturador

## 1.0.0

Versión inicial del módulo.

### Arquitectura
- Base del motor de facturación electrónica para Ecuador.
- Arquitectura modular con enfoque Hexagonal y DDD pragmático.
- Separación de dominio, aplicación, infraestructura y adapters.
- Workflow externalizado mediante Apache Camel XML DSL.

### Persistencia
- PostgreSQL con JPA/Hibernate.
- Flyway para control de esquema.
- Validación del esquema mediante `ddl-auto=validate`.

### Facturación electrónica
- Base para generación y validación XML.
- Modelo de DocumentDefinition y versionamiento de definiciones.
- Integración con servicios SRI mediante configuración de ambientes.
- Base para firma electrónica con DSS.
- Gestión de configuración externa.
- Soporte para snapshot histórico y workflow persistente en el diseño.

### Seguridad
- Integración preparada para Spring Security, OAuth2/OIDC, JWT y Keycloak.
- Secretos sensibles integrados mediante Vault/configtree.

### Mensajería
- Integración con `factucore-messaging`.
- Kafka como broker configurado actualmente.
- Publicación de eventos de notificación.

### API
- Integración de SpringDoc OpenAPI `2.9.1`.
- Swagger UI habilitado.
- Especificación OpenAPI JSON/YAML habilitada.

### Versiones principales
- Java 21
- Spring Boot 3.5.16
- Apache Camel 4.10.7
- SpringDoc OpenAPI 2.9.1
- EU DSS 6.5
- PDFBox 3.0.5
