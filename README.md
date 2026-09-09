# FactuCore

Sistema genérico de facturación electrónica para Ecuador, orientado a la integración con el **Servicio de Rentas Internas (SRI)**.

FactuCore será diseñado desde cero con una arquitectura modular, mantenible y preparada para adaptarse a cambios en las especificaciones técnicas de facturación electrónica.

---

## 🎯 Objetivo

Desarrollar una plataforma de facturación electrónica que permita gestionar el ciclo completo de los comprobantes electrónicos y su comunicación con el SRI.

El sistema estará diseñado como un producto **genérico y configurable**, evitando incorporar lógica específica de un cliente dentro del núcleo del sistema.

---

## 🧾 Funcionalidades principales

FactuCore tendrá como objetivo soportar:

- Facturas electrónicas.
- Notas de crédito.
- Notas de débito.
- Comprobantes de retención.
- Liquidaciones de compra.
- Guías de remisión.
- Gestión de numeración y secuencias.
- Generación de XML.
- Validación de comprobantes.
- Firma electrónica.
- Envío de comprobantes al SRI.
- Consulta de recepción.
- Consulta de autorización.
- Gestión del estado de los comprobantes.
- Generación de representación PDF.
- Envío de comprobantes por correo electrónico.
- Almacenamiento y consulta de documentos electrónicos.
- Gestión de certificados de firma electrónica.
- Configuración por contribuyente, establecimiento y punto de emisión.

---

## ⚙️ Motor XML configurable

Uno de los objetivos principales de FactuCore es disponer de un motor de generación XML configurable.

La intención es que los cambios normales en las estructuras de los comprobantes electrónicos del SRI puedan ser administrados mediante configuración, evitando modificar el código de negocio cada vez que cambie una versión del esquema.

La configuración podrá contemplar, entre otros aspectos:

- Tipo de comprobante.
- Versión del XSD.
- Estructura XML.
- Elementos y atributos.
- Orden de los elementos.
- Campos obligatorios.
- Cardinalidad.
- Tipos de datos.
- Origen de los datos.
- Transformaciones.
- Reglas de generación.

El código Java proporcionará los mecanismos de transformación y procesamiento, mientras que las reglas y estructuras configurables podrán mantenerse en la base de datos.

---

## 🔐 Firma electrónica

FactuCore implementará la firma electrónica de los comprobantes de acuerdo con los requerimientos establecidos para facturación electrónica del SRI.

La arquitectura deberá permitir trabajar con:

- Certificados PKCS#12 (`.p12`).
- XAdES-BES.
- XAdES 1.3.2.
- Firmas XML de tipo ENVELOPED.
- RSA 2048.
- RSA-SHA1, cuando corresponda al perfil requerido por el SRI.

La implementación será validada mediante pruebas con comprobantes reales y los servicios del SRI.

---

## 🏛️ Arquitectura

FactuCore será desarrollado utilizando:

- **Domain-Driven Design (DDD)**.
- **Arquitectura Hexagonal**.
- Separación clara entre dominio, aplicación e infraestructura.
- Principios de bajo acoplamiento.
- Interfaces/puertos para las dependencias externas.
- Adaptadores para tecnologías e integraciones externas.

El dominio no dependerá directamente de:

- Spring.
- JPA/Hibernate.
- PostgreSQL.
- REST.
- XML.
- SRI.
- Librerías de firma electrónica.
- JasperReports.
- Docker.

Las tecnologías externas serán implementadas mediante adaptadores.

---

## 📦 Estructura del repositorio

FactuCore utilizará un único repositorio Git:

```text
factucore/
│
├── api/
│   ├── domain/
│   ├── application/
│   ├── adapters/
│   └── bootstrap/
│
├── web/
│
├── database/
│
├── docker/
│
└── docs/
```

### `api/`

Backend de FactuCore.

Será desarrollado con Java y Spring Boot utilizando Maven y arquitectura DDD + Hexagonal.

### `web/`

Frontend de FactuCore.

Será desarrollado utilizando React y Node.js/npm.

### `database/`

Scripts relacionados con PostgreSQL, incluyendo posteriormente:

- Migraciones.
- Estructura de tablas.
- Datos iniciales.
- Configuraciones necesarias para la base de datos.

### `docker/`

Configuraciones para ejecutar los diferentes componentes mediante Docker.

### `docs/`

Documentación técnica y funcional del proyecto.

---

## 🛠️ Tecnologías base

La plataforma tendrá como base tecnológica:

### Backend

- Java 17 LTS.
- Spring Boot 3.x.
- Maven.
- Spring Framework.
- Hibernate/JPA.
- PostgreSQL.
- EU DSS para firma electrónica, sujeto a validación del perfil requerido por el SRI.
- JasperReports para generación de documentos PDF.

### Frontend

- React.
- Node.js LTS.
- npm.

### Infraestructura

- PostgreSQL.
- Docker.
- Git.

---

## 🔄 Flujo general de emisión

El flujo principal previsto será:

```text
Solicitud de emisión
        │
        ▼
Validación del comprobante
        │
        ▼
Generación XML
        │
        ▼
Firma electrónica
        │
        ▼
Envío al SRI
        │
        ▼
Recepción / validación
        │
        ▼
Autorización
        │
        ▼
Generación PDF
        │
        ▼
Almacenamiento
        │
        ▼
Envío al cliente
```

---

## 🧩 Diseño genérico

FactuCore no estará diseñado exclusivamente para una empresa determinada.

La configuración permitirá administrar diferentes:

- Contribuyentes.
- Establecimientos.
- Puntos de emisión.
- Certificados.
- Ambientes.
- Series y secuencias.
- Clientes.
- Productos.
- Impuestos.
- Tarifas.
- Formas de pago.
- Configuraciones de comprobantes.

El objetivo es que el mismo producto pueda ser utilizado por diferentes empresas sin modificar el núcleo de negocio.

---

## 🗄️ Persistencia

La base de datos principal será PostgreSQL.

La persistencia estará aislada mediante adaptadores para evitar que las reglas de negocio dependan directamente de PostgreSQL o de JPA/Hibernate.

---

## 🌐 Integración con el SRI

FactuCore tendrá una capa de integración con los servicios electrónicos del SRI.

Esta integración será considerada un adaptador externo, permitiendo mantener aislado el dominio de las particularidades técnicas de los servicios del SRI.

La integración contemplará el ciclo de:

1. Recepción del comprobante.
2. Generación del XML.
3. Firma electrónica.
4. Envío.
5. Consulta de recepción.
6. Consulta de autorización.
7. Actualización del estado.
8. Almacenamiento de respuestas.

---

## 📚 Documentación

La carpeta `docs/` contendrá progresivamente la documentación del proyecto:

- Arquitectura.
- DDD.
- Bounded Contexts.
- Agregados.
- Casos de uso.
- Puertos y adaptadores.
- Modelo de datos.
- Integración SRI.
- Firma electrónica.
- API REST.
- Decisiones arquitectónicas.

---

## 🚧 Estado del proyecto

FactuCore se encuentra en **fase de diseño arquitectónico**.

Antes de comenzar la implementación se definirán:

1. Arquitectura general.
2. Bounded Contexts.
3. Modelo de dominio.
4. Agregados.
5. Reglas de negocio.
6. Casos de uso.
7. Puertos.
8. Adaptadores.
9. Módulos Maven.
10. Modelo de base de datos.
11. API REST.
12. Frontend.

La implementación se realizará progresivamente después de validar cada decisión arquitectónica.

---

## 📌 Principios del proyecto

FactuCore seguirá principalmente estos principios:

- **DDD primero.**
- **Separación de responsabilidades.**
- **Dominio independiente de frameworks.**
- **Bajo acoplamiento.**
- **Alta cohesión.**
- **Configuración sobre código cuando sea apropiado.**
- **Integraciones externas aisladas mediante adaptadores.**
- **Código genérico y reutilizable.**
- **Preparado para evolución de las especificaciones del SRI.**
- **Un único repositorio Git para todo el producto.**