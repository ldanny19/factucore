# Vault de FactuCore

Vault se ejecuta como servidor persistente (no usar modo `-dev`). En Docker Compose, el almacenamiento interno se encuentra en `/vault/data` y se persiste en `${FACTUCORE_HOME}/vault/data`. El archivo de inicializacion se guarda en `${FACTUCORE_HOME}/secrets/vault-init.json`.

## Instalacion inicial

1. Copiar `compose/.env.example` a `compose/.env` y configurar al menos `FACTUCORE_HOME` y `FACTUCORE_VERSION`.
2. Ejecutar desde la raiz del repositorio con Bash (Linux o Git Bash):

   ```bash
   ./instalar-factucore.sh
   ```

3. El instalador solicita las contrasenas de los secretos declarados en `compose/vault/config/secretos.conf`, inicia Vault, almacena los secretos y luego despliega el resto de servicios.

El catalogo central define las rutas Vault, los usuarios tecnicos y las descripciones de las credenciales. No contiene contrasenas. Las contrasenas se solicitan ocultas, se mantienen en memoria en el instalador y se transmiten por stdin al contenedor; no se escriben en archivos temporales.

## Instalaciones existentes

Si existe `vault-init.json`, el instalador no solicita contrasenas ni regenera secretos. Arranca Vault y el stack reutilizando las credenciales existentes. Si el almacenamiento existe pero falta el archivo de inicializacion, se detiene para evitar una reinicializacion accidental.

Si Vault ya estaba inicializado y faltan secretos requeridos, el bootstrap falla sin generarlos ni sobrescribirlos. Se necesita una recuperacion controlada.

## Rutas de secretos

El catalogo versionado esta en `compose/vault/config/secretos.conf`. Los secretos se guardan en KV v2 bajo `secret/factucore` y el Vault Agent los renderiza en el volumen de secretos de runtime para PostgreSQL y las aplicaciones.

Las claves de unseal y el root token se almacenan en el directorio persistente de secretos con permisos restrictivos. Ese directorio debe protegerse y respaldarse de acuerdo con la politica operativa del ambiente.

## Operacion normal

Despues de una instalacion exitosa, se puede seguir utilizando:

```bash
docker compose --env-file compose/.env -f compose/docker-compose.yml up -d
```

No se deben versionar el root token, las claves de unseal, tokens del Agent ni contrasenas.
