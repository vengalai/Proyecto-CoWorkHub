# CoWorkHub

API REST con Spring Boot para la gestion de reservas de una red de coworking, con
interfaz web en JavaScript servida por la propia aplicacion.

> Estado: en construccion por fases. Este README se completa al final con los datos de
> conexion, usuarios de prueba y enlaces de Swagger UI.

## Requisitos

- Java 21 o superior
- MySQL 8 (instalado o en Docker)
- No hace falta instalar Maven: se incluye el wrapper (`mvnw` / `mvnw.cmd`)

## Base de datos

```sql
CREATE DATABASE coworkhub CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'coworkhub'@'localhost' IDENTIFIED BY 'coworkhub';
GRANT ALL PRIVILEGES ON coworkhub.* TO 'coworkhub'@'localhost';
FLUSH PRIVILEGES;
```

Alternativa con Docker:

```bash
docker run -d --name coworkhub-mysql -e MYSQL_DATABASE=coworkhub -e MYSQL_USER=coworkhub -e MYSQL_PASSWORD=coworkhub -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -v coworkhub-datos:/var/lib/mysql mysql:8.4
```

## Ejecutar

```bash
./mvnw spring-boot:run        # Linux / macOS
.\mvnw.cmd spring-boot:run    # Windows
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/doc/swagger-ui.html`
- Salud: `http://localhost:8080/actuator/health`
- Aplicacion web: `http://localhost:8080/`

## Documentacion del proyecto

- [Analisis, modelo y trazabilidad](docs/01-analisis.md)
