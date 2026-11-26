# Plan de fases

Este plan resume el orden recomendado para construir y revisar el proyecto. El usuario
decide cuándo hacer cada commit; el asistente no crea commits.

| Fase | Alcance | Verificación |
|---|---|---|
| **1. Esqueleto y análisis** | Proyecto Maven, clase principal, configuración inicial, Actuator, `.gitignore`, README y trazabilidad de los 14 escenarios. | Compilación, pruebas, arranque y `GET /actuator/health` |
| **2. Modelo JPA y datos** | 9 entidades, enums, repositorios y datos de prueba mínimos. | Arranque con H2; MySQL se verifica aparte |
| **3. Errores y catálogos** | Errores comunes, validación y CRUD de sedes/salas/equipamiento/servicios/planes. | Pruebas de validación y reglas RN-11/RN-12 |
| **4. Miembros y consumo** | Alta de miembro y cuenta, filtros, suspensión/activación, baja y consumo mensual. | Filtros y cálculo de consumo |
| **5. Disponibilidad y reservas** | Disponibilidad, creación, servicios, costos, cancelación, estados y bloqueo concurrente. | Reglas RN-01 a RN-10 |
| **6. Seguridad** | BCrypt, JWT de 60 minutos, roles, permisos y acceso del dueño. | Accesos `401` y `403` |
| **7. Swagger** | Documentación interactiva y esquema JSON de la API. | Swagger UI y `/v3/api-docs` |
| **8. Interfaz web** | Interfaz adaptable: login, catálogos, disponibilidad, reservas, miembros y consumo. | Recorrido manual desde el navegador |
| **9. Pruebas y entrega** | Pruebas automatizadas, colección Postman y guía para ejecutar el proyecto. | `mvnw test` y colección Postman |
| **10. Verificación final** | Revisión de consultas, solicitudes simultáneas y arranque limpio. | Lista final de la rúbrica |

## Convenciones

- Paquetes, clases, atributos, métodos y mensajes en español. Se conservan los nombres de tablas y columnas en inglés mediante `@Table` y `@Column`, según la decisión acordada.
- Paquetes: `presentacion` (`controlador`, `dto`, `excepcion`), `negocio` (`model`, `servicio`, `implementacion`, `excepcion`), `persistencia` (`repositorio`), `config` y `seguridad`.
- `src/main/resources/import.sql` contiene los datos de MySQL y `import-h2.sql` es su versión compatible con H2.
- DTO de entrada con `@Valid`, DTO de salida como `record`; nunca entidades en el JSON.
- Inyección por constructor y `@Transactional` en las escrituras.
- Los 14 escenarios se mantienen como criterios de aceptación; la cobertura automatizada y la ejecución manual se reportan por separado.
