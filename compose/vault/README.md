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
