\set ON_ERROR_STOP on

\set facturador_password `cat /vault/secrets/postgres_facturador_password`
\set auth_password `cat /vault/secrets/postgres_auth_password`
\set notificaciones_password `cat /vault/secrets/postgres_notificaciones_password`

SELECT format('CREATE ROLE usr_factucore LOGIN PASSWORD %L;', :'facturador_password')
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_roles
    WHERE rolname = 'usr_factucore'
) \gexec

SELECT format('CREATE ROLE usr_auth LOGIN PASSWORD %L;', :'auth_password')
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_roles
    WHERE rolname = 'usr_auth'
) \gexec

SELECT format('CREATE ROLE usr_notificaciones LOGIN PASSWORD %L;', :'notificaciones_password')
WHERE NOT EXISTS (
    SELECT 1
    FROM pg_roles
    WHERE rolname = 'usr_notificaciones'
) \gexec

CREATE DATABASE db_factucore OWNER usr_factucore;
CREATE DATABASE db_auth OWNER usr_auth;
CREATE DATABASE db_notificaciones OWNER usr_notificaciones;
