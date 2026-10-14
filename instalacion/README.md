# Instalacion y secretos de FactuCore

## Separacion de responsabilidades

- `infraestructura/vault/config/secretos.conf`: catalogo canonico de rutas de Vault y usuarios tecnicos. No contiene contrasenas y no forma parte de la configuracion operativa externalizada de las aplicaciones.
- `configuraciones/`: configuracion operativa, plantillas, definiciones XSD y recursos que se copian a las carpetas externalizadas de ejecucion.
- `infraestructura/vault/`: configuracion del servidor Vault, Vault Agent, politicas y scripts propios de Vault.
- `infraestructura/postgres/`: inicializacion de roles y bases de datos PostgreSQL.
- `infraestructura/keycloak/README.md`: responsabilidades y notas de provisionamiento de Keycloak, independientes del mecanismo de despliegue.
- `instalar-factucore.sh`: orquestador actual de la instalacion inicial con Docker Compose.
- `compose/`: definicion del despliegue Docker Compose y variables especificas de ese mecanismo.

El catalogo de secretos vive junto a la configuracion de infraestructura de Vault, no dentro de `configuraciones/` ni de `compose/`. El instalador de Compose lo copia a `$FACTUCORE_HOME/vault/config/secretos.conf` y copia los archivos de infraestructura a las rutas persistentes configuradas por `FACTUCORE_HOME`. Una futura implementacion de Kubernetes/Helm puede reutilizar el catalogo, aunque necesitara su propio adaptador de despliegue y mecanismo de provisionamiento.

## Instalacion inicial con Docker Compose

1. Copia `compose/.env.example` a `compose/.env`.
2. Configura `FACTUCORE_HOME` y `FACTUCORE_VERSION`.
3. Desde la raiz del repositorio, ejecuta en Linux o Git Bash:

   ```bash
   ./instalar-factucore.sh
   ```

El instalador solicita las contrasenas de los secretos catalogados, las oculta al escribir y las transmite por entrada estandar al proceso que las almacena en Vault. No las agrega a archivos de configuracion.

## Instalaciones existentes

Si existe el archivo de inicializacion de Vault, el instalador reutiliza los secretos y no pide nuevas contrasenas. Si el almacenamiento de Vault existe pero falta su archivo de inicializacion, se detiene para evitar reinicializar una instancia potencialmente existente. Si falta algun secreto en una instalacion existente, se detiene y requiere recuperacion controlada.

## Operacion habitual

Tras una instalacion completada, Docker Compose puede seguir utilizandose directamente:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
```

No versionar el root token, las claves de unseal, tokens del Agent ni contrasenas.
