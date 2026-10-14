# Instalacion y secretos de FactuCore

## Separacion de responsabilidades

- `configuracion/secretos.conf`: catalogo canonico de rutas de Vault y usuarios tecnicos. No contiene contrasenas.
- `infraestructura/vault/`: configuracion del servidor Vault, Vault Agent, politicas y scripts propios de Vault.
- `infraestructura/postgres/`: inicializacion de roles y bases de datos PostgreSQL.
- `instalar-factucore.sh`: orquestador actual de la instalacion inicial con Docker Compose.
- `compose/`: definicion del despliegue Docker Compose y variables especificas de ese mecanismo.

El catalogo de secretos y los archivos de infraestructura ya no viven dentro de `compose/`. El instalador de Compose los instala en las rutas persistentes configuradas por `FACTUCORE_HOME`. Una futura implementacion de Kubernetes/Helm puede reutilizar el catalogo, aunque necesitara su propio adaptador de despliegue y mecanismo de provisionamiento.

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
