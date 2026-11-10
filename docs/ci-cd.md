# CI/CD de FactuCore

## Orquestación

El workflow `.github/workflows/ci-cd.yml` es el punto de entrada del CI/CD del monorepo. Se ejecuta en `push` a `develop` y `main` cuando cambian archivos de un módulo o los workflows, y también permite ejecución manual mediante `workflow_dispatch`.

La detección de cambios separa estos módulos:

- `factucore-messaging`: librería Maven compartida.
- `facturador`: servicio Maven y único módulo con CD configurado.
- `notificacion`: servicio Maven.
- `auditoria`: servicio Maven que también consume mensajería.
- `web`: aplicación React/TypeScript.

La carpeta `auditoria` existe en el repositorio y consume `factucore-messaging`; por eso también participa en la orquestación, además de los cuatro módulos inicialmente considerados.

## Dependencia Maven compartida

`facturador`, `notificacion` y `auditoria` dependen de:

- Group ID: `ec.dalara.factucore`
- Artifact ID: `factucore-messaging`
- Version: `1.0.0`

No se utiliza GitHub Packages ni se presupone un repositorio Maven central. El workflow compila `factucore-messaging` y publica el JAR como GitHub Actions Artifact con nombre `factucore-messaging`. Los módulos consumidores descargan ese artefacto dentro de la misma ejecución y lo instalan en el repositorio Maven local del runner con `maven-install-plugin` antes de ejecutar `mvn clean verify`.

Los artefactos son temporales de la ejecución (retención de siete días en la plantilla), no un repositorio permanente. Por eso el orquestador compila Messaging cuando cambia la librería o cuando cambia cualquiera de sus consumidores: así el JAR requerido siempre está disponible en el mismo run, incluso si Messaging no tuvo cambios propios. Los artefactos de los demás módulos tienen nombres únicos para evitar colisiones entre jobs.

## Paralelismo y dependencias

- Messaging se ejecuta primero cuando es necesario.
- Facturador, Notificación y Auditoría esperan a Messaging y pueden ejecutarse en paralelo.
- Web es independiente y puede compilarse en paralelo.
- Si Messaging falla, sus consumidores no continúan.
- Una ejecución manual ejecuta todos los módulos.

## Docker y despliegue

Docker no compila Maven. La imagen de Facturador consume el JAR producido por CI JAVA BUILD y descargado como artefacto; el mismo nombre de artefacto se pasa a la etapa Docker.

El CD configurado corresponde únicamente a Facturador y se ejecuta en `main`, después de construir y publicar la imagen. No se configura CD para `develop`. Notificación y Auditoría ejecutan CI Maven; sus Dockerfiles/despliegues quedan fuera de este alcance hasta adaptar y validar sus contratos de imagen.

## Plantillas reutilizables

Los workflows Java consumen `ldanny19/devops-pipeline-templates`. La versión utilizada incorpora soporte para descargar una dependencia Maven desde un GitHub Actions Artifact, instalarla localmente y manejar nombres de artefactos únicos para los módulos y la etapa Docker.

La plantilla se encuentra en la rama `feature/maven-local-artifact-dependency` del repositorio de plantillas, commit `d8c17e7211e2d96f8e38f049d15b34322798b085`. Antes de integrar el cambio de FactuCore en `main`, debe integrarse también esa rama de plantillas y mantenerse el pin por SHA.

## Validación pendiente

Los archivos se han preparado y versionado en `develop`, pero todavía requieren una ejecución real de GitHub Actions para validar:

- detección de cambios por rutas;
- publicación y descarga del artefacto Messaging entre workflows reutilizables;
- compilación Maven de los tres consumidores;
- construcción de la imagen de Facturador;
- lint y build de Web;
- análisis Trivy y despliegue de Facturador en `main`.

No se considera validado el pipeline hasta revisar esos resultados.
