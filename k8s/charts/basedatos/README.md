# PostgreSQL de FactuCore

Este chart migra únicamente el contenedor `db_factucore` de Docker Compose.

## Recursos creados

- Un `StatefulSet` de una réplica.
- Un Service headless interno llamado `srv-db-factucore`.
- Un PVC por réplica, generado por `volumeClaimTemplates` y montado en
  `/var/lib/postgresql/data`.

El Pod se llamará `db-factucore-0`. El volumen que se cree para él no se elimina
cuando el Pod se recrea, por lo que los datos sobreviven a ese evento.

## Credenciales e inicialización

Vault Agent renderiza las credenciales en el PVC `pvc-vault-runtime-secrets`.
El Pod de PostgreSQL monta ese PVC como solo lectura y usa
`POSTGRES_PASSWORD_FILE`, igual que Compose. Un init container espera a que
Vault Agent haya escrito las credenciales; no se crea ni se requiere un Secret
de Kubernetes.

El instalador crea `cm-db-factucore-init` directamente desde
`infraestructura/postgres/init/01-crear-bases-servicios.sql` y lo monta en
`/docker-entrypoint-initdb.d`. El SQL mantiene una sola fuente canónica fuera
del chart. PostgreSQL ejecuta este script solo cuando inicializa un volumen de
datos vacío; en un PVC ya inicializado no se vuelve a ejecutar automáticamente.

El PVC de secretos usa `ReadWriteOnce`; por eso el Pod de PostgreSQL se agenda
en el mismo nodo que Vault Agent para poder leer los archivos.
