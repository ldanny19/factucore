# Instalación y secretos de FactuCore

## Separación de responsabilidades

- `infraestructura/vault/config/secretos.yml`: catálogo canónico YAML de rutas de Vault, usuarios técnicos y descripciones. No contiene contraseñas ni valores sensibles.
- `$FACTUCORE_HOME/vault/config/secretos.conf`: archivo plano generado automáticamente por el instalador a partir del YAML para que los scripts POSIX de Vault puedan consumir los metadatos sin instalar un parser YAML en la imagen oficial de Vault.
- `configuraciones/`: configuración versionada de referencia; el instalador copia los archivos faltantes a las carpetas de ejecución sin sobrescribir ajustes locales.
- `FACTUCORE_HOME`: raíz única de los datos locales. Cada módulo/servicio tiene su propia carpeta y dentro de ella su configuración o persistencia.
- `infraestructura/vault/`: configuración del servidor Vault, Vault Agent, políticas y scripts propios de Vault.
- `infraestructura/postgres/`: inicialización de roles y bases de datos PostgreSQL.
- `infraestructura/keycloak/README.md`: responsabilidades y notas de provisionamiento de Keycloak, independientes del mecanismo de despliegue.
- `instalar-factucore.sh`: orquestador actual de la instalación inicial con Docker Compose.
- `compose/`: definición del despliegue Docker Compose y variables específicas de ese mecanismo.


## Estructura local de FACTUCORE_HOME

```text
FACTUCORE_HOME/
├── vault/
│   ├── config/             # Configuración y catálogo de Vault
│   ├── scripts/            # Bootstrap y scripts operativos
│   ├── policy/             # Políticas
│   ├── agent/              # Configuración del Vault Agent
│   ├── data/               # Persistencia de Vault
│   ├── secrets/            # Inicialización de Vault y token persistente del Agent
│   ├── agent-token/        # Token compartido con Vault Agent dentro de Docker
│   └── runtime-secrets/    # Secretos renderizados para consumo de servicios
├── base_datos/
│   └── postgres/\n│       ├── data/           # Datos persistentes de PostgreSQL
│           └── init/       # SQL de inicialización
├── mensajeria/\n│   ├── kafka/data/         # Datos persistentes de Kafka\n│   └── config/             # Configuración compartida de mensajería
├── facturador/
│   ├── config/             # application.yml, mensajes y workflow
│   ├── jasper/             # Plantillas RIDE
│   ├── documentos/         # XSD y comprobantes/XML generados
│   ├── certificados/       # Certificados de firma
│   ├── imagenes/           # Recursos gráficos
│   └── logs/               # Logs del Facturador
├── notificacion/
│   ├── config/             # Configuración y plantillas de correo
│   └── logs/               # Logs de Notification
└── messaging/config/       # Configuración compartida de mensajería
```

Keycloak guarda su base `db_auth` en PostgreSQL, por lo que su persistencia queda bajo `base_datos/postgres/data`; no se crea un almacenamiento duplicado para Keycloak. La carpeta `vault/runtime-secrets` contiene credenciales renderizadas en texto legible para los procesos que las consumen: protege su acceso y no la sincronices ni la versionas. El instalador crea las carpetas y copia configuraciones de referencia solo cuando faltan; no sobrescribe configuraciones locales ni migra automáticamente datos de las rutas antiguas.

El catálogo canónico es YAML. El instalador requiere Docker Compose v2 y `yq` v4 (mikefarah/yq); valida el catálogo y genera `secretos.conf` en la carpeta externalizada. También copia el YAML para mantener disponible la fuente utilizada. Los scripts de Vault continúan leyendo el archivo plano generado, por lo que no se necesita modificar la imagen oficial de Vault. Una futura implementación de Kubernetes/Helm puede reutilizar el YAML, con su propio adaptador de despliegue.

## Instalación inicial con Docker Compose

1. Copia `compose/.env.example` a `compose/.env`.
2. Configura `FACTUCORE_HOME` y `FACTUCORE_VERSION`.
3. Instala Docker Compose v2 y `yq` v4 (mikefarah/yq).
4. Desde la raíz del repositorio, ejecuta en Linux o Git Bash:

   ```bash
   ./instalar-factucore.sh
   ```

El instalador solicita las contraseñas de los secretos catalogados, las oculta al escribir y las transmite por entrada estándar al proceso que las almacena en Vault. No las agrega a archivos de configuración.


## Migración de instalaciones existentes

Antes de levantar la nueva definición Compose, detén los contenedores para evitar copiar datos activos. Si existen estas rutas antiguas, mueve su contenido a las nuevas ubicaciones sin borrar copias de respaldo:

- `%FACTUCORE_HOME%\\data\\postgres` → `%FACTUCORE_HOME%\\postgres\\data`
- `%FACTUCORE_HOME%\\secrets` → `%FACTUCORE_HOME%\\vault\\secrets`

También migra `%FACTUCORE_HOME%\\\\kafka\\\\data` a `%FACTUCORE_HOME%\\\\mensajeria\\\\kafka\\\\data` y `%FACTUCORE_HOME%\\\\messaging\\\\config` a `%FACTUCORE_HOME%\\\\mensajeria\\\\config`.\n\nEn Windows, con Docker Compose ejecutado desde la raíz del repositorio:

```powershell
$env:FACTUCORE_HOME = "C:\\factucore"  # Usa aquí la ruta configurada en compose/.env
docker compose --env-file compose/.env -f compose/docker-compose.yml down
New-Item -ItemType Directory -Force "$env:FACTUCORE_HOME\\postgres" | Out-Null
New-Item -ItemType Directory -Force "$env:FACTUCORE_HOME\\vault" | Out-Null
Move-Item "$env:FACTUCORE_HOME\\data\\postgres" "$env:FACTUCORE_HOME\\postgres\\data"
Move-Item "$env:FACTUCORE_HOME\\secrets" "$env:FACTUCORE_HOME\\vault\\secrets"
```

Si ya existe el destino, no ejecutes `Move-Item` a ciegas: compara las carpetas y realiza una migración controlada. El instalador se detiene si detecta datos en las rutas antiguas y la nueva ruta está vacía, para no iniciar accidentalmente un PostgreSQL o Vault nuevos. Conserva el respaldo hasta verificar la instalación.

## Instalaciones existentes

Si existe el archivo de inicialización de Vault, el instalador reutiliza los secretos y no pide nuevas contraseñas. Si el almacenamiento de Vault existe pero falta su archivo de inicialización, se detiene para evitar reinicializar una instancia potencialmente existente. Si falta algún secreto en una instalación existente, se detiene y requiere recuperación controlada.

## Operación habitual

Tras una instalación completada, Docker Compose puede seguir utilizándose directamente:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
```

No versionar el root token, las claves de unseal, tokens del Agent ni contraseñas.
