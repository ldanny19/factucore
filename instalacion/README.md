# Instalación de FactuCore

Esta guía describe la instalación inicial y la operación local de FactuCore mediante Docker Compose. Se utiliza **Git Bash** en Windows y se ejecutan los comandos desde la raíz del repositorio.

## 1. Separación entre código y datos

El repositorio contiene código fuente, Docker Compose, scripts y configuraciones de referencia. Los datos persistentes, secretos, certificados, documentos generados, logs y configuraciones operativas deben permanecer fuera del repositorio.

La ubicación externa se define mediante la variable de entorno **`FACTUCORE_HOME`**. Cada instalación puede elegir la carpeta raíz que necesite el cliente; los archivos Compose y los scripts deben resolver las rutas a partir de esta variable y no depender de una ruta absoluta escrita en el código.

Para el entorno de desarrollo actual se utiliza el valor definido en `compose/.env`. Si se cambia la ubicación, actualiza esa variable en dicho archivo antes de ejecutar la instalación o Compose. No es necesario cambiar las rutas de los volúmenes individualmente.

La estructura bajo `FACTUCORE_HOME` se organiza por responsabilidad:

```text
FACTUCORE_HOME/
├── vault/
│   ├── config/
│   ├── scripts/
│   ├── policy/
│   ├── agent/
│   ├── data/
│   ├── secrets/
│   ├── agent-token/
│   └── runtime-secrets/
├── base_datos/
│   └── postgres/
│       ├── data/
│       └── init/
├── mensajeria/
│   ├── kafka/
│   │   └── data/
│   └── config/
├── facturador/
│   ├── config/
│   ├── jasper/
│   ├── documentos/
│   ├── certificados/
│   ├── imagenes/
│   └── logs/
└── notificacion/
    ├── config/
    └── logs/
```

Keycloak utiliza PostgreSQL para su base de datos, por lo que su persistencia queda bajo `base_datos/postgres/data`; no necesita un volumen de datos duplicado. La carpeta `vault/runtime-secrets` puede contener secretos renderizados legibles por los procesos que los consumen: protege su acceso y nunca la versionas ni la sincronices a repositorios públicos.

## 2. Requisitos

- Git Bash.
- Docker Engine o Docker Desktop con Docker Compose v2.
- `yq` v4 de Mike Farah, requerido por el instalador para procesar el catálogo YAML de secretos.
- Acceso al repositorio y sus imágenes/servicios requeridos.

Comprueba que `FACTUCORE_HOME` esté definido en `compose/.env` y que la ruta elegida sea accesible para Docker. El archivo `compose/.env.example` sirve como plantilla; crea `compose/.env` a partir de él si todavía no existe y configura las variables necesarias para tu entorno.

## 3. Ejecutar la instalación

Desde Git Bash, sitúate en la raíz del repositorio, donde se encuentran `instalar-factucore.sh` y la carpeta `compose`.

Comprueba la rama y la ubicación actual, y luego ejecuta el instalador:

```bash
git branch --show-current
pwd
bash ./instalar-factucore.sh
```

El instalador prepara las carpetas externas y copia configuraciones de referencia solo cuando el archivo de destino no existe. No sobrescribe configuraciones operativas locales. También genera el catálogo plano que necesitan los scripts de Vault a partir de `infraestructura/vault/config/secretos.yml`.

Cuando se soliciten secretos, introdúcelos de forma interactiva. El instalador no debe incorporarlos a archivos versionados ni imprimir sus valores. No compartas contraseñas, tokens, claves de unseal ni archivos de inicialización de Vault en logs o conversaciones.

Si el instalador informa de rutas antiguas, almacenamiento de Vault existente sin inicialización o secretos ausentes, **detente y revisa el estado antes de continuar**. No borres carpetas ni inicialices de nuevo una instancia existente para intentar resolverlo.

## 4. Validar y ejecutar Docker Compose

Docker Compose se ejecuta desde la raíz del workspace del repositorio. Las rutas de los volúmenes se resuelven desde `FACTUCORE_HOME`, de modo que los datos permanezcan fuera del workspace.

Primero valida la configuración efectiva:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml config --quiet
```

Si la validación termina correctamente, inicia los servicios:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
```

Consulta el estado y los logs cuando sea necesario:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml ps
docker compose --env-file compose/.env -f compose/docker-compose.yml logs --tail=100
```

Para detener los servicios sin eliminar los volúmenes ni las carpetas externas:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml down
```

No utilices `down -v` como comando rutinario: antes de eliminar volúmenes o datos persistentes, confirma qué almacenamiento se eliminaría y que existe un respaldo válido.

## 5. Configuración externa de mensajería

El archivo de referencia de mensajes se encuentra en `configuraciones/messaging/message.properties`. El instalador lo copia, si falta, a `FACTUCORE_HOME/mensajeria/config/message.properties`. Compose monta esa carpeta como solo lectura para los servicios que la utilizan.

Los cambios operativos deben realizarse en la copia externa, no en el repositorio. El resolver consulta el catálogo externo y conserva el catálogo empaquetado como respaldo según su configuración de caché.

## 6. Secretos y configuración de Vault

Responsabilidades principales:

- `infraestructura/vault/config/secretos.yml`: catálogo canónico de rutas, usuarios técnicos y descripciones; no debe contener contraseñas.
- `FACTUCORE_HOME/vault/config/secretos.conf`: archivo plano generado por el instalador para los scripts POSIX de Vault.
- `infraestructura/vault/`: configuración de referencia, políticas, Vault Agent y scripts.
- `configuraciones/`: configuraciones versionadas de referencia, copiadas sin sobrescribir los archivos locales existentes.

No guardes secretos en `.env`, YAML versionado, SQL, Dockerfiles ni código fuente. El archivo del certificado de firma y los secretos operativos deben mantenerse fuera del repositorio.

## 7. Migrar instalaciones existentes

Antes de cambiar la distribución de carpetas, detén los servicios y realiza un respaldo. Si una instalación anterior guardaba información en rutas distintas, identifica el origen y el destino a partir de `FACTUCORE_HOME` y migra los datos de forma controlada.

No muevas ni combines directorios a ciegas si el destino ya existe. Compara el contenido, conserva una copia de respaldo y verifica la persistencia de PostgreSQL, Kafka y Vault antes de eliminar cualquier copia antigua. El instalador puede detenerse al detectar datos heredados para evitar iniciar accidentalmente una base de datos o un Vault vacíos.

## 8. Operación habitual

Desde la raíz del repositorio, con `compose/.env` configurado:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
docker compose --env-file compose/.env -f compose/docker-compose.yml ps
```

La guía general, arquitectura y estado del proyecto se documentan en el [README principal](../README.md). Este documento se reserva para la instalación, la configuración del entorno y las tareas operativas.
