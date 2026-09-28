# Vault FactuCore

Vault se ejecuta en modo servidor normal, con almacenamiento persistente en el directorio configurado por `FACTUCORE_HOME`.

No usar `server -dev`.

## Inicialización

La primera ejecución requiere:

1. Iniciar Vault.
2. Ejecutar `vault operator init`.
3. Guardar las claves de unseal y el token inicial fuera del repositorio.
4. Hacer unseal.
5. Crear el motor KV v2 `secret`.
6. Cargar los secretos de PostgreSQL y Keycloak.

Las claves de inicialización, tokens y secretos nunca se versionan.

## Estructura de secretos

- `secret/factucore/postgresql`
- `secret/factucore/keycloak`

Los valores concretos dependen del ambiente.


## Bootstrap automatizado

El `docker-compose.yml` solamente levanta la infraestructura. La inicialización y configuración de Vault se realiza mediante:

`compose/vault/scripts/bootstrap-vault.ps1`

El script:

1. Verifica que Vault esté inicializado y desellado.
2. Habilita/verifica el engine KV v2 en `secret/`.
3. Carga las credenciales de PostgreSQL y Keycloak desde variables de entorno o las solicita de forma interactiva.
4. Aplica la policy `factucore-agent`.
5. Crea o reutiliza el token del Vault Agent.
6. Guarda el token del Agent únicamente en `FACTUCORE_HOME/secrets/vault_agent_token.txt`.
7. Valida que el token pueda leer los secretos requeridos.

Variables opcionales:

- `FACTUCORE_VAULT_CONTAINER`
- `FACTUCORE_VAULT_ROOT_TOKEN`
- `FACTUCORE_POSTGRES_PASSWORD`
- `FACTUCORE_KEYCLOAK_ADMIN_PASSWORD`

No se deben versionar el root token, las claves de unseal, el token del Agent ni las contraseñas.

Ejecución desde PowerShell:

```powershell
.\\compose\\vault\\scripts\\bootstrap-vault.ps1
```

Después del bootstrap se puede levantar el resto del stack mediante Docker Compose.
