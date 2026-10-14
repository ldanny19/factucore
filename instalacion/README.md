# Instalación y secretos de FactuCore

## Separación de responsabilidades

- `infraestructura/vault/config/secretos.yml`: catálogo canónico YAML de rutas de Vault, usuarios técnicos y descripciones. No contiene contraseñas ni valores sensibles.
- `$FACTUCORE_HOME/vault/config/secretos.conf`: archivo plano generado automáticamente por el instalador a partir del YAML para que los scripts POSIX de Vault puedan consumir los metadatos sin instalar un parser YAML en la imagen oficial de Vault.
- `configuraciones/`: configuración operativa, plantillas, definiciones XSD y recursos que se copian a las carpetas externalizadas de ejecución.
- `infraestructura/vault/`: configuración del servidor Vault, Vault Agent, políticas y scripts propios de Vault.
- `infraestructura/postgres/`: inicialización de roles y bases de datos PostgreSQL.
- `infraestructura/keycloak/README.md`: responsabilidades y notas de provisionamiento de Keycloak, independientes del mecanismo de despliegue.
- `instalar-factucore.sh`: orquestador actual de la instalación inicial con Docker Compose.
- `compose/`: definición del despliegue Docker Compose y variables específicas de ese mecanismo.

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

## Instalaciones existentes

Si existe el archivo de inicialización de Vault, el instalador reutiliza los secretos y no pide nuevas contraseñas. Si el almacenamiento de Vault existe pero falta su archivo de inicialización, se detiene para evitar reinicializar una instancia potencialmente existente. Si falta algún secreto en una instalación existente, se detiene y requiere recuperación controlada.

## Operación habitual

Tras una instalación completada, Docker Compose puede seguir utilizándose directamente:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
```

No versionar el root token, las claves de unseal, tokens del Agent ni contraseñas.
