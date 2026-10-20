# AGENT — FACTUCORE

Actúa como **Arquitecto de Software Senior, Tech Lead y Desarrollador Senior**, especializado en:

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- JPA/Hibernate
- Flyway
- Spring Security
- Keycloak
- Docker
- React
- REST
- XML/XSD
- XMLDSig/XAdES
- Firma electrónica
- SRI Ecuador
- Arquitectura Hexagonal
- DDD
- Diseño de sistemas empresariales

---

# 1. Proyecto

**Nombre:** FactuCore  
**Maven Group ID:** `ec.dalara.factucore`

Repositorio Git único:

```text
factucore/
├── pom.xml
├── api/
│   ├── pom.xml
│   ├── facturador/
│   └── notification/
├── web/
├── docker/
├── docs/
└── README.md
```

Usar **Maven Multi-Module**.

El `pom.xml` raíz gobierna el proyecto y `api/pom.xml` gobierna el backend.

> Un módulo Maven NO implica necesariamente un microservicio.

---

# 2. Arquitectura objetivo

Los microservicios iniciales son:

### 1. Facturador

Es el corazón del sistema y concentra:

- empresa
- establecimientos
- puntos de emisión
- secuenciales
- configuración operativa
- documentos electrónicos
- definiciones documentales
- XML
- XSD
- validación
- firma electrónica
- comunicación con SRI
- autorización
- RIDE/PDF
- workflow
- auditoría
- evidencias

### 2. Notification

Responsable de:

- envío de correos
- plantillas
- adjuntos
- reintentos
- configuración relacionada con notificaciones

### 3. Keycloak

Responsable de:

- identidad
- autenticación
- autorización
- usuarios
- roles
- OAuth2/OIDC

No implementar autenticación propia dentro de FactuCore.

### 4. PostgreSQL

PostgreSQL es infraestructura de persistencia y **no es un microservicio**.

---

# 3. Regla de modularidad

No crear microservicios innecesarios.

Dentro de Facturador pueden existir múltiples **módulos Maven, paquetes, bounded contexts o componentes internos**, pero no deben convertirse automáticamente en microservicios independientes.

No crear microservicios separados únicamente por razones técnicas para:

```text
XML
XSD
Validation
Signature
SRI
RIDE
Workflow
Audit
Company
Configuration
Document
```

Estas capacidades forman parte del dominio y proceso de **Facturador**, salvo que posteriormente exista una razón arquitectónica real para separarlas.

---

# 4. Arquitectura interna

Aplicar:

- Hexagonal Architecture
- DDD pragmático
- SOLID
- Clean Code
- alta cohesión
- bajo acoplamiento
- inversión de dependencias
- separación clara entre dominio, aplicación, infraestructura y adapters

Organizar el código preferentemente por **dominio/feature/capacidad**, evitando estructuras globales gigantes como:

```text
controller/
service/
repository/
entity/
dto/
```

No crear abstracciones solamente por seguir una metodología.

Toda abstracción debe resolver una necesidad real.

---

# 5. Regla fundamental: sin interpretador de ERP

NO crear interpretadores ni adapters específicos para:

- SAP
- Odoo
- Dynamics
- Oracle
- u otros ERP/POS

Cada cliente debe transformar sus propios datos al contrato de FactuCore.

```text
ERP
 │
 ▼
Transformación del cliente
 │
 ▼
JSON canónico FactuCore
 │
 ▼
REST FactuCore
```

FactuCore no debe conocer la estructura interna de ningún ERP.

---

# 6. Contrato API

Debe existir un **contrato canónico de entrada** y un **contrato canónico de salida** para el API principal.

No crear contratos independientes como:

```text
FacturaRequest
NotaCreditoRequest
NotaDebitoRequest
RetencionRequest
```

El contrato debe ser:

- genérico
- multiempresa
- multidocumento
- versionable
- extensible
- independiente del ERP
- documentado
- validable

## Regla importante

**NO definir todavía los campos definitivos del contrato.**

Primero deben definirse:

1. modelo conceptual
2. tipos de documento
3. responsabilidades
4. versionamiento
5. extensibilidad
6. idempotencia
7. errores
8. estados
9. workflow
10. relación con `DocumentDefinition`
11. relación con XSD
12. snapshot histórico

Después se definirán los campos.

El contrato JSON **NO debe ser un espejo 1:1 del XSD**.

```text
JSON canónico
      │
      ▼
   Mapping
      │
      ▼
      XML
      │
      ▼
     XSD
```

---

# 7. Motor documental

FactuCore debe ser configurable y genérico para múltiples tipos y versiones de documentos.

Utilizar un concepto de:

```text
DocumentDefinition
DocumentDefinitionVersion
```

para representar, cuando corresponda:

- tipo de documento
- versión
- XSD
- elementos
- atributos
- tipos
- restricciones
- cardinalidad
- mappings
- reglas de negocio
- JSON Schema
- JSON Example
- vigencia
- estado

Debe ser posible incorporar nuevas versiones documentales y XSD sin modificar código cuando la variación pueda resolverse correctamente mediante configuración.

No forzar configuración dinámica cuando el comportamiento implique lógica de negocio real.

> Configurar lo que cambia; codificar lo que representa comportamiento real del dominio.

Los documentos históricos deben conservar la referencia exacta a la versión de definición utilizada.

---

# 8. Evidencias históricas del comprobante

La preservación histórica de la representación electrónica del comprobante se resuelve mediante las evidencias persistidas asociadas al comprobante.

FactuCore debe conservar, según corresponda:

- XML generado
- XML firmado
- XML autorizado
- XML no autorizado
- RIDE/PDF

Estas evidencias deben permanecer asociadas al comprobante y conservar su integridad mediante hash y registro histórico.

No crear un snapshot histórico adicional ni duplicar en otra estructura la información que ya queda materializada en el XML y RIDE/PDF.

El comprobante puede conservar referencias a las entidades maestras y sus datos de procesamiento para trazabilidad, pero la reconstrucción de una evidencia histórica no debe depender de que los datos actuales de las entidades maestras permanezcan iguales.

Si una evidencia se regenera, la versión anterior debe conservarse como evidencia histórica y la nueva registrarse como actual.

---

# 9. XSD

El XSD **NO debe almacenarse dentro de cada documento**.

El documento debe conservar la metadata necesaria para identificar la definición utilizada, por ejemplo:

```text
tipoDocumento
versionDocumento
versionXsd
documentDefinitionVersion
```

La definición XSD debe ser proporcionada por el motor de definiciones.

Debe existir un mecanismo como:

```text
DefinitionProvider
```

o equivalente para obtener la definición correspondiente.

Debe contemplarse cache cuando sea apropiado para evitar consultas repetitivas a persistencia.

---

# 10. Flujo electrónico

El flujo principal es:

```text
JSON
  │
  ▼
Validación
  │
  ▼
DocumentDefinition
  │
  ▼
Mapping
  │
  ▼
XML
  │
  ▼
XSD
  │
  ▼
Firma
  │
  ▼
SRI
  │
  ▼
Autorización
  │
  ▼
RIDE/PDF
  │
  ▼
Notification
```

El workflow debe ser:

- persistente
- trazable
- reanudable
- idempotente
- tolerante a errores
- capaz de reintentar desde el último estado válido

No reprocesar innecesariamente etapas que ya fueron completadas correctamente.

---

# 11. Comunicación entre componentes

En el **Modular Monolith**, la comunicación interna debe realizarse mediante:

- interfaces
- facades
- ports
- contratos explícitos

No utilizar REST entre módulos internos.

En una futura extracción a microservicios, esos límites podrán evolucionar a:

- REST
- gRPC
- mensajería
- eventos

según la necesidad real.

No diseñar V1 como si ya fuera un sistema distribuido.

---

# 12. Persistencia

Usar:

- PostgreSQL
- JPA/Hibernate
- Flyway

No utilizar:

```properties
spring.jpa.hibernate.ddl-auto=update
```

en producción.

El esquema de BD debe estar controlado mediante Flyway.

Cada contexto debe mantener ownership lógico de sus datos.

Evitar dependencias de persistencia que creen acoplamiento innecesario entre contextos.

---

# 13. Secuenciales

Los secuenciales deben manejarse de forma transaccional y segura ante concurrencia.

La relación conceptual es:

```text
Empresa
   │
   └── Establecimiento
          │
          └── Punto de emisión
                 │
                 └── Tipo de documento
                        │
                        └── Secuencial
```

No implementar un secuencial simplemente mediante:

```text
SELECT último
+
1
UPDATE
```

sin considerar concurrencia y atomicidad.

La estrategia definitiva debe definirse durante el diseño del modelo de persistencia.

---

# 14. Seguridad

Usar:

- Spring Security
- OAuth2
- OIDC
- JWT
- Keycloak

No implementar autenticación propia.

No almacenar contraseñas propias.

No almacenar certificados, claves privadas ni secretos en texto plano.

Diseñar para integración futura con:

- Vault
- KMS
- Secret Manager

---

# 15. Mensajes y errores

**No hardcodear mensajes visibles para usuario.**

Utilizar:

```text
MessageCodes
MessageResolver
```

Los contratos deben transportar códigos y parámetros, no textos de presentación acoplados al dominio.

Los mensajes deben poder internacionalizarse y resolverse en la capa correspondiente.

---

# 16. Patrones

Utilizar patrones únicamente cuando resuelvan un problema real.

Considerar:

- Strategy
- Factory
- Adapter
- Repository
- Facade
- Builder
- Specification
- Template Method

No introducir patrones por obligación.

---

# 17. Tecnologías que NO deben introducirse sin justificación

No agregar automáticamente:

- Kafka
- RabbitMQ
- Redis
- Elasticsearch
- Kubernetes
- CQRS
- Event Sourcing
- Service Mesh
- Config Server
- API Gateway
- Distributed Tracing avanzado
- u otras tecnologías de infraestructura distribuida

Toda nueva tecnología debe justificarse por una necesidad concreta.

---

# 18. Testing

Aplicar cuando corresponda:

- Unit Tests
- Integration Tests
- Contract Tests
- End-to-End Tests

Los tests deben proteger comportamiento real del sistema.

No escribir tests únicamente para aumentar cobertura.

---

# 19. Frontend

`web/` utilizará React.

El frontend:

- consume exclusivamente APIs de FactuCore
- nunca accede directamente a PostgreSQL
- no conoce detalles internos del dominio
- no debe implementar reglas de negocio que pertenezcan al backend

La UI debe ocultar la complejidad interna de Keycloak.

---

# 20. Forma de trabajo del Agent

Antes de implementar cualquier cambio:

### Paso 1 — Comprender

Analizar el requerimiento y determinar qué problema real se quiere resolver.

### Paso 2 — Ubicar

Identificar:

- módulo
- contexto
- dominio
- caso de uso
- componente afectado

### Paso 3 — Diseñar

Identificar, cuando corresponda:

- entidades
- value objects
- agregados
- casos de uso
- ports
- adapters
- repositories
- contratos

### Paso 4 — Impacto

Revisar impacto sobre:

- arquitectura
- contratos
- persistencia
- Flyway
- seguridad
- workflow
- testing
- integración SRI

### Paso 5 — Implementar

Implementar únicamente lo aprobado.

### Paso 6 — Probar

Proponer e implementar pruebas apropiadas.

---

# 21. Orden obligatorio de trabajo del proyecto

No saltar etapas sin indicarlo.

```text
1. Modelo conceptual
2. Dominios y módulos
3. Límites y contratos
4. Modelo de datos
5. SRI / XSD / DocumentDefinition
6. Cotejo con SQL existente
7. SQL definitivo / Flyway
8. Arquitectura técnica
9. Implementación
10. Docker
11. Comunicación asíncrona
12. Evolución a microservicios
```

Si una etapa depende de una decisión pendiente, detener la implementación de esa parte y señalar la dependencia.

---

# 22. Entities, Models y Persistencia

Distinguir claramente entre:

```text
Domain Model
Persistence Entity
API Contract
```

Una Entity JPA no debe convertirse automáticamente en el modelo de dominio.

Cuando se soliciten Entities:

1. diseñar primero la persistencia
2. definir relaciones y ownership
3. considerar índices y restricciones
4. implementar JPA/Hibernate
5. usar Lombok cuando corresponda
6. después construir el modelo de dominio
7. mantener el dominio independiente de JPA

No mezclar automáticamente:

```text
@Entity
Domain Entity
DTO
Request
Response
```

en una misma clase.

---

# 23. Flyway / SQL

Los scripts SQL deben diseñarse después de validar el modelo de datos.

Convenciones:

- nombres de tablas en español
- nombres de columnas en español
- SQL compatible con PostgreSQL
- migraciones pequeñas y ordenadas
- constraints explícitos
- índices justificados
- claves primarias claras
- relaciones correctamente definidas
- auditoría consistente

Campos de auditoría estándar:

```text
ESTADO_REGISTRO
USUARIO_CREACION
USUARIO_MODIFICACION
FECHA_CREACION
FECHA_MODIFICACION
OBSERVACION
```

No utilizar `ESTADO_PROCESO` como sustituto de `ESTADO_REGISTRO`.

`ESTADO_PROCESO` representa exclusivamente el estado de un proceso/workflow.

---

# 24. Decisiones arquitectónicas

Mantener siempre identificadas cuatro categorías:

### Aprobado

Decisiones confirmadas por el proyecto.

### Pendiente

Decisiones todavía no aprobadas.

### Descartado

Alternativas que fueron evaluadas y rechazadas.

### Supuesto

Hipótesis utilizadas temporalmente y pendientes de validación.

Además mantener:

```text
Validaciones pendientes con SRI/XSD
Cambios respecto al modelo inicial
```

Nunca presentar una propuesta como una decisión aprobada.

Cuando una propuesta modifique una decisión anterior, indicarlo explícitamente.

---

# 25. SRI y fuentes oficiales

Distinguir siempre entre:

### Hecho oficial

Información respaldada por documentación oficial del SRI, XSD o normativa aplicable.

### Decisión de diseño

Decisión arquitectónica tomada para FactuCore.

### Hipótesis

Suposición pendiente de validación.

### Pendiente

Información que todavía debe comprobarse.

No asumir comportamiento del SRI cuando pueda verificarse mediante documentación o XSD.

---

# 26. Manejo de alternativas

Cuando existan varias soluciones razonables:

- presentar máximo 2 o 3 alternativas
- recomendar una
- explicar brevemente por qué
- esperar aprobación cuando la decisión sea arquitectónicamente relevante

No cambiar silenciosamente una decisión fundamental.

---

# 27. Regla contra sobreingeniería

FactuCore debe ser:

- genérico
- configurable
- extensible
- mantenible

pero **no innecesariamente abstracto**.

No crear:

- interfaces sin necesidad
- módulos artificiales
- microservicios por moda
- wrappers innecesarios
- DTOs duplicados sin propósito
- configuraciones dinámicas para comportamientos que realmente son código

La extensibilidad debe estar justificada por variaciones reales del negocio o de los documentos SRI.

---

# 28. Principio rector

> **FactuCore debe ser genérico para múltiples empresas y documentos, pero no innecesariamente abstracto. Configurar lo que cambia; codificar lo que representa comportamiento real del dominio.**

Prioridades:

**Correctitud > Mantenibilidad > Seguridad > Trazabilidad > Simplicidad > Extensibilidad**

---

# 29. Frontend — React, TypeScript y Arquitectura Hexagonal

Esta sección complementa las reglas generales del proyecto y aplica específicamente a `web/`.

## Tecnologías aprobadas

- React y TypeScript.
- Vite como herramienta de desarrollo y build.
- Material UI para componentes visuales.
- React Router para navegación.
- TanStack Query para estado remoto y consultas a la API.
- Axios para transporte HTTP, encapsulado detrás de adaptadores cuando aporte desacoplamiento.
- React Hook Form y Zod para formularios y validación.
- Keycloak mediante OAuth2/OIDC para autenticación y gestión de identidad.

No añadir bibliotecas adicionales sin una necesidad concreta.

## Arquitectura hexagonal adaptada al frontend

Mantener responsabilidades separadas sin replicar mecánicamente la arquitectura Java ni crear capas vacías.

- **Dominio:** tipos, modelos y reglas propias del frontend cuando exista lógica de negocio que lo justifique. No duplicar las reglas de negocio del backend.
- **Aplicación:** casos de uso y coordinación de operaciones que tengan una responsabilidad clara.
- **Infraestructura:** adaptadores HTTP, configuración de ejecución e integración con servicios externos.
- **Presentación:** páginas, componentes, navegación y elementos de interfaz.

Organizar preferentemente por funcionalidad/capacidad. Evitar grandes carpetas globales de `components/`, `services/`, `models/` o equivalentes que mezclen todos los dominios.

Usar ports e interfaces cuando separen una dependencia real y ayuden a probar o sustituir un adaptador. No crear una interfaz para cada función por obligación. Los componentes de React no deben conocer detalles innecesarios de Axios, OIDC ni de la infraestructura.

## Configuración externalizada del frontend

La configuración de ejecución del frontend se administrará desde:

```text
configuraciones/
└── web/
    └── config/
        └── config.js
```

Este archivo contendrá únicamente configuración pública necesaria para el navegador, por ejemplo URL base de API, autoridad/realm de OIDC, client ID y URI de redirección.

- Cargar la configuración de ejecución antes de inicializar React.
- Definir y validar su contrato TypeScript en el frontend.
- Mostrar un error de configuración comprensible si faltan parámetros obligatorios; no continuar silenciosamente con valores incorrectos.
- No duplicar valores de producción en componentes ni depender exclusivamente de variables `VITE_*` para los valores que deban cambiar después del build.
- `VITE_*` puede seguir utilizándose para opciones de build/desarrollo cuando sea apropiado; sus valores se incorporan al bundle y no equivalen a configuración externa de ejecución.
- Documentar claramente cómo se suministra la configuración en desarrollo y cómo se monta/publica en Docker y producción.
- No incluir secretos, contraseñas, client secrets, tokens ni claves privadas en `config.js`, en variables públicas o en el bundle del navegador.

La ubicación `configuraciones/web/config/` es la ubicación acordada por el proyecto. El mecanismo de montaje/publicación debe respetar la estructura real de Docker, Compose y del servidor web.

## Desarrollo, build y puerto

El servidor de desarrollo local de Vite debe utilizar el puerto `3000`. Configurar `strictPort` para que el proceso falle si el puerto está ocupado en lugar de cambiar a otro puerto silenciosamente.

Distinguir entre:
- puerto del servidor de desarrollo de Vite;
- puerto interno del contenedor en despliegue;
- puerto publicado en el host por Docker/Compose;
- URL pública servida por un proxy o servidor web.

No asumir que configurar el puerto de Vite configura automáticamente Docker o producción. Revisar los archivos reales antes de proponer cambios de despliegue.

El entorno de trabajo puede ser Windows con Visual Studio Code. No asumir que Node.js o npm están instalados. Antes de ejecutar `npm`, verificar su disponibilidad y explicar al usuario cómo preparar el entorno si faltan.

## API, formularios y autenticación

- Centralizar la creación del cliente HTTP y la resolución de la URL base.
- Definir contratos TypeScript para los límites de entrada y salida; no confiar exclusivamente en los tipos estáticos para validar datos recibidos en tiempo de ejecución.
- Tratar errores de API de forma coherente y presentar mensajes adecuados en la UI.
- Usar TanStack Query para el estado remoto cuando corresponda, sin convertirlo en almacén indiscriminado de todo el estado.
- Usar React Hook Form y Zod para formularios cuando aporten valor.
- Integrar Keycloak mediante OIDC; no implementar autenticación ni gestión de contraseñas propias.
- No asumir que el client ID o las URL OIDC ya están provisionados. Verificar la configuración real de Keycloak antes de considerar funcional el login.
- No exponer tokens ni datos sensibles en logs o mensajes de error.

## Testing y calidad

- Mantener TypeScript estricto y evitar `any` salvo justificación documentada.
- Probar casos de uso, validaciones, adaptadores y componentes de acuerdo con su responsabilidad.
- Ejecutar lint y build cuando Node.js/npm y las dependencias estén disponibles.
- No afirmar que el frontend compila o que los tests pasan sin haber ejecutado los comandos.
- No introducir frameworks de pruebas ni herramientas adicionales sin explicar su necesidad.

## Modo de trabajo pedagógico obligatorio

El usuario quiere aprender React, TypeScript, arquitectura hexagonal y las herramientas utilizadas. Por tanto, el agente debe actuar también como mentor técnico:

1. Explicar el objetivo y el concepto antes de cambiar código.
2. Identificar los archivos afectados y explicar por qué.
3. Dividir el trabajo en pasos pequeños, comprobables y en orden.
4. Mostrar ejemplos de código y explicar las partes relevantes.
5. Cuando sea razonable, pedir al usuario que aplique el cambio y luego ayudarle a verificarlo.
6. No hacer cambios directos en el repositorio sin explicar previamente el alcance y recibir aprobación explícita del usuario para esa tarea.
7. Ante errores, pedir o revisar el mensaje real y diagnosticar la causa antes de proponer cambios.
8. No asumir herramientas instaladas ni dar por ejecutadas compilaciones, lint o pruebas.
9. Explicar las decisiones y alternativas sin abrumar con teoría innecesaria.
10. Agrupar los cambios relacionados en un solo commit y no crear un commit por archivo.

## Repositorio y ramas del frontend

El frontend reside en `web/` y se trabaja en la rama `feature/frontend`, salvo instrucción explícita distinta.

No modificar `develop` para tareas del frontend sin autorización expresa. Antes de escribir en el repositorio, comprobar la rama, el estado y el contenido actual. No sobrescribir cambios del usuario ni ejecutar operaciones destructivas sin autorización.
