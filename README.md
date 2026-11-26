# CoWorkHub

CoWorkHub es una aplicación para administrar una red de espacios de coworking. Tiene una
API REST hecha con Spring Boot y una interfaz web en español que se abre desde el
navegador. Desde la aplicación se pueden consultar sedes, salas, miembros, disponibilidad,
reservas y consumo mensual.

## Requisitos

- Java 21 o superior.
- MySQL 8 para ejecutar con la configuración normal, o H2 para probar sin instalar MySQL.
- No hace falta instalar Maven: el proyecto incluye el wrapper `mvnw`.

## Ejecutar con H2

Esta es la forma más rápida de abrir el proyecto sin crear una base de datos MySQL:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=h2"
```

En Linux o macOS:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

La base H2 queda en memoria y se carga con datos de ejemplo. Al detener la aplicación,
esos datos se pierden.

## Ejecutar con MySQL

Primero crea la base y el usuario:

```sql
CREATE DATABASE coworkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'coworkhub'@'localhost' IDENTIFIED BY 'coworkhub';
GRANT ALL PRIVILEGES ON coworkhub.* TO 'coworkhub'@'localhost';
FLUSH PRIVILEGES;
```

Después inicia la aplicación:

```powershell
.\mvnw.cmd spring-boot:run
```

La configuración educativa usa `ddl-auto=create`: cada arranque elimina y vuelve a crear
las tablas y carga los datos de prueba. No se debe usar con información que se quiera
conservar. La clave JWT incluida también es de ejemplo y no debe usarse en un sistema real.

## Abrir la aplicación

- Interfaz web: <http://localhost:8080/>
- Rutas de la API: por ejemplo, `/api/sedes`, `/api/reservas` y `/api/miembros`.
- Swagger UI: <http://localhost:8080/doc/swagger-ui.html>
- Esquema OpenAPI: <http://localhost:8080/v3/api-docs>
- Estado del servicio: <http://localhost:8080/actuator/health>
- Consola H2 (solo con el perfil `h2`): <http://localhost:8080/h2-console>

## Cuentas de ejemplo

La contraseña de las cuentas de prueba es `coworkhub123`.

| Usuario | Rol | Uso |
|---|---|---|
| `admin` | Administrador | Administra catálogos, miembros y reservas. |
| `recepcion` | Recepción | Gestiona miembros y reservas. |
| `ana` | Miembro | Consulta y gestiona sus propias reservas. |
| `bruno` | Miembro | Consulta y gestiona sus propias reservas. |

Son cuentas únicamente para desarrollo y pruebas.

## Verificar el proyecto

En Windows:

```powershell
.\mvnw.cmd test
```

En Linux o macOS:

```bash
./mvnw test
```

Las pruebas usan H2 y verifican los datos iniciales y los 14 escenarios de aceptación.
La colección de Postman para probar llamadas comunes está en
[`docs/postman/CoWorkHub.postman_collection.json`](docs/postman/CoWorkHub.postman_collection.json).

## Carpetas principales

- `src/main/java`: código Java de la aplicación.
- `src/main/resources`: configuración y datos iniciales de MySQL y H2.
- `src/main/resources/static`: interfaz web (HTML, CSS y JavaScript sin proceso de build).
- `src/test`: pruebas automatizadas.
- `docs/01-analisis.md`: reglas, modelo de datos y los 14 escenarios.
- `docs/02-plan.md`: fases y convenciones del proyecto.
