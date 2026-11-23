# Plan de fases

Cada fase se implementa, se verifica y se reporta antes de pasar a la siguiente. El
usuario revisa y decide cuándo commitear (el agente no hace commits).

| Fase | Alcance | Verificación |
|---|---|---|
| **1. Esqueleto y análisis** | Proyecto Maven (`pom.xml`, wrapper), clase principal, `application.properties`, salud de Actuator, `.gitignore`, `README.md` inicial y `docs/01-analisis.md` con trazabilidad de los 14 escenarios. | `mvnw compile`, `mvnw test`, arranque y `GET /actuator/health` |
| **2. Modelo JPA y datos** | 9 entidades + 4 enums, repositorios, `import.sql` con los mínimos del enunciado. | Arranque contra MySQL, tablas creadas, conteos de `import.sql` |
| **3. Errores y catálogos** | Cuerpo de error único, excepciones propias, CRUD de sedes/salas/equipamiento/servicios/planes con validación y RN-11/RN-12. | Escenario 12 y pruebas de catálogos |
| **4. Miembros y consumo** | Alta transaccional `Member`+`UserAccount`, filtros, suspender/activar, baja, consumo mensual. | Escenario 14 y cálculo de consumo |
| **5. Disponibilidad y reservas** | Disponibilidad, alta de reserva con servicios, RN-01 a RN-10, cálculo de costos, cancelación, transiciones, concurrencia. | Escenarios 1–8, 11 y 13 |
| **6. Seguridad** | BCrypt, `POST /auth/login`, JWT 60 min, filtro stateless, roles y dueño, `401`/`403`. | Escenarios 9 y 10 + matriz rol/recurso |
| **7. Swagger** | Grupos, descripciones, ejemplos, errores, Authorize y `docs/openapi.json`. | Swagger UI y JSON exportado |
| **8. Interfaz web** | SPA en `src/main/resources/static`: login, catálogos, disponibilidad, reservas, miembros, consumo; menú por rol. | Recorrido manual de los 14 escenarios desde el navegador |
| **9. Pruebas y entrega** | Suite de aceptación automatizada, colección Postman/Insomnia, README definitivo. | `mvnw test` completo y ejecución de la colección |
| **10. Verificación final** | N+1 con `show-sql`, seis solicitudes simultáneas, arranque limpio (`ddl-auto=create` + `import.sql`). | Checklist de la rúbrica |

## Convenciones

- Paquetes: `presentacion` (`controlador`, `dto`, `excepcion`), `negocio` (`model`, `service`, `excepcion`), `persistence` (`repository`), `config`, `jwt`.
- Nombres de clases y métodos en español; tablas y columnas en inglés.
- DTO de entrada con `@Valid`, DTO de salida como `record`; nunca entidades en el JSON.
- Inyección por constructor y `@Transactional` en las escrituras.
- Cada regla de negocio tiene al menos una prueba automatizada.
