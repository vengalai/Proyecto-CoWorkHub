# CoWorkHub — Análisis, modelo y trazabilidad

Documento de análisis del proyecto final. Complementa el enunciado e incorpora el
requisito adicional acordado: **interfaz web con JavaScript** además de la API REST.
Los 14 escenarios de aceptación del enunciado se mantienen sin cambios.

El **código se escribe en español** (clases, atributos, métodos y variables); las
**tablas y columnas conservan nombres en inglés** mediante `@Table` y `@Column`, para no
alterar el modelo de datos ni el seed.

---

## 1. Actores y casos de uso

| Actor | Descripción | Casos de uso principales |
|---|---|---|
| **ADMIN** | Empleado con control total del sistema. Único rol que administra catálogos. | Gestionar sedes, salas, equipamiento, servicios y planes. Registrar, actualizar, suspender, reactivar y eliminar miembros. Gestionar reservas y consultar consumo de cualquier miembro. |
| **RECEPTIONIST** | Empleado de recepción. Opera el día a día con miembros y reservas, sin tocar catálogos. | Registrar y mantener miembros. Crear reservas a nombre de cualquier miembro. Confirmar, cancelar y completar reservas. Consultar disponibilidad y consumo de cualquier miembro. |
| **MEMBER** | Cliente del coworking con plan de membresía y cuenta propia. | Iniciar sesión, consultar catálogos y disponibilidad, crear/ver/modificar/cancelar **sus** reservas y ver **su** consumo. |
| *Público* | Visitante sin token. | Iniciar sesión, consultar salud del servicio y documentación. |

### Casos de uso (resumen)

1. **Autenticarse** (`POST /auth/login`): valida credenciales BCrypt y devuelve un JWT de 60 minutos.
2. **Consultar perfil** (`GET /auth/perfil`): usuario, rol y `miembroId` del token.
3. **Gestionar catálogos**: CRUD de sedes, salas (con equipamiento), equipamiento, servicios adicionales y planes. Escritura solo `ADMIN`.
4. **Gestionar miembros**: alta transaccional de `Miembro` + `CuentaUsuario`, listado con filtros, actualización, suspensión/reactivación y baja.
5. **Consultar disponibilidad**: salas libres de una sede para fecha y rango horario, con filtros de capacidad y equipamiento.
6. **Reservar**: alta de reserva con servicios; el sistema valida RN-01 a RN-07 y devuelve el costo calculado.
7. **Consultar reservas**: por id, por miembro, por sala y fecha, o por estado.
8. **Modificar servicios** de una reserva `PENDING` y recalcular el costo.
9. **Cambiar estado**: confirmar, cancelar (con RN-08) y completar.
10. **Consultar consumo mensual**: horas incluidas, usadas, restantes y excedentes.
11. **Operar la interfaz web**: el mismo conjunto de casos, desde el navegador, respetando el rol del usuario autenticado.

---

## 2. Glosario

| Término | Definición |
|---|---|
| **Sede** | Ubicación física del coworking: ciudad, dirección y horario de apertura y cierre. Agrupa las salas reservables. |
| **Sala** | Espacio reservable de una sede, con tipo, capacidad, tarifa por hora y equipamiento. |
| **Equipamiento** | Recurso compartido (proyector, pizarra, monitor…) que puede asignarse a varias salas. |
| **Servicio adicional** | Prestación opcional que se cobra por cantidad en una reserva (catering, café…). Su precio se congela al reservar. |
| **Cuenta de usuario** | Credenciales de acceso (nombre de usuario, contraseña BCrypt y rol) asociadas a un miembro. |
| **Reserva** | Bloque de uso de una sala por un miembro, con fecha, hora de inicio y fin, asistentes, servicios adicionales y estado (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`). |
| **Plan** | Membresía contratada por un miembro. Define las horas incluidas por mes, el descuento sobre horas excedentes y el máximo de reservas activas simultáneas. |
| **Horas incluidas** | Horas que el plan cubre sin costo adicional en el mes calendario. Se consumen con las reservas del miembro y no generan cargo mientras haya saldo. |
| **Horas excedentes** | Horas reservadas por encima de las incluidas en el mes. Se cobran a `tarifaHora × (1 − descuentoExcedente/100)` de la sala reservada. |
| **Solapamiento** | Cruce temporal entre dos reservas `PENDING` o `CONFIRMED` de la misma sala. Dos intervalos que solo se tocan en un extremo (una termina cuando la otra empieza) **no** se solapan. |
| **Cargo por cancelación** | Importe que se cobra al cancelar según la anticipación: `0` con 24 h o más, `50 %` del costo de la sala entre 24 h y 2 h, y cancelación rechazada con menos de 2 h o si la reserva ya comenzó. |

---

## 3. Modelo entidad-relación

### 3.1 Convención de nombres

| Elemento | Idioma | Ejemplo |
|---|---|---|
| Clases, atributos, métodos y variables | Español | `Sala.tarifaHora`, `Reserva.calcularCosto()` |
| Tablas y columnas | Inglés, fijadas con `@Table` / `@Column` | `rooms.hourly_rate` |
| Valores de enum, rutas y códigos | Los del enunciado | `EstadoReserva.PENDING`, `/api/reservas`, `RN-01` |
| Mensajes de error y documentación | Español | `"La sala ya está reservada en ese horario"` |

El enunciado §2 pedía atributos en inglés; se prioriza la rúbrica §4 por decisión del
proyecto. La base conserva nombres en inglés para no modificar el modelo de datos ni el
seed, y la correspondencia queda explícita en la tabla 3.3.

### 3.2 Diagrama

```mermaid
erDiagram
    SEDE ||--o{ SALA : contiene
    SALA }o--o{ EQUIPAMIENTO : equipada_con
    PLAN_MEMBRESIA ||--o{ MIEMBRO : suscribe
    MIEMBRO |o--o| CUENTA_USUARIO : inicia_sesion_con
    MIEMBRO ||--o{ RESERVA : realiza
    SALA ||--o{ RESERVA : reservada_en
    RESERVA ||--o{ DETALLE_RESERVA : contiene
    SERVICIO_ADICIONAL ||--o{ DETALLE_RESERVA : cobrado_en

    SEDE {
        bigint id PK
        varchar nombre
        varchar ciudad
        varchar direccion
        time horaApertura
        time horaCierre
    }
    SALA {
        bigint id PK
        bigint sede_id FK
        varchar nombre
        varchar tipo
        int capacidad
        decimal tarifaHora
        boolean activa
    }
    EQUIPAMIENTO {
        bigint id PK
        varchar nombre
    }
    SALA_EQUIPAMIENTO {
        bigint sala_id FK
        bigint equipamiento_id FK
    }
    PLAN_MEMBRESIA {
        bigint id PK
        varchar nombre
        int horasIncluidasMes
        int descuentoExcedente
        int maxReservasActivas
    }
    MIEMBRO {
        bigint id PK
        bigint plan_id FK
        varchar numeroDocumento
        varchar nombreCompleto
        varchar email
        varchar estado
    }
    CUENTA_USUARIO {
        bigint id PK
        bigint miembro_id FK
        varchar nombreUsuario
        varchar contrasena
        varchar rol
    }
    RESERVA {
        bigint id PK
        bigint sala_id FK
        bigint miembro_id FK
        datetime inicio
        datetime fin
        int asistentes
        varchar estado
        decimal costoSala
        decimal costoServicios
        decimal costoTotal
        decimal cargoCancelacion
    }
    SERVICIO_ADICIONAL {
        bigint id PK
        varchar nombre
        decimal precioUnitario
    }
    DETALLE_RESERVA {
        bigint id PK
        bigint reserva_id FK
        bigint servicio_id FK
        int cantidad
        decimal precioUnitarioAplicado
    }
```

### 3.3 Correspondencia clase ↔ tabla

| Clase Java | Tabla | Notas |
|---|---|---|
| `Sede` | `locations` | Catálogo raíz; agrupa salas. |
| `Sala` | `rooms` | FK `location_id`; N:N con equipamiento. |
| `Equipamiento` | `equipment` | Catálogo compartido entre salas. |
| — | `room_equipment` | Tabla intermedia `sala_equipamiento`; sin entidad propia. |
| `PlanMembresia` | `membership_plans` | Define cupo de horas, descuento y tope de reservas. |
| `Miembro` | `members` | FK `plan_id`; datos personales y estado. |
| `CuentaUsuario` | `user_accounts` | FK única `member_id`; credenciales y rol. |
| `Reserva` | `bookings` | FK `room_id` y `member_id`; costos y estado. |
| `ServicioAdicional` | `additional_services` | Catálogo de servicios cobrados por cantidad. |
| `DetalleReserva` | `booking_items` | FK `booking_id` y `service_id`; cantidad y precio aplicado. |

### 3.4 Decisiones de mapeo JPA

| Relación | Lado dueño | `fetch` | `cascade` / `orphanRemoval` | Justificación |
|---|---|---|---|---|
| `Sede` 1→N `Sala` | `Sala` (`@ManyToOne`, atributo `sede`) | `LAZY` | ninguno | La sala conoce su sede; borrar una sede no debe arrastrar salas (RN-12 lo impide antes). |
| `Sala` N↔N `Equipamiento` | `Sala` (`@ManyToMany` + `@JoinTable(name = "room_equipment")`) | `LAZY` | ninguno | El equipamiento es un catálogo compartido; borrar una sala no borra equipamiento. |
| `PlanMembresia` 1→N `Miembro` | `Miembro` (`@ManyToOne`, atributo `plan`) | `LAZY` | ninguno | El plan no contiene a sus miembros; RN-12 impide borrar un plan en uso. |
| `Miembro` 1↔1 `CuentaUsuario` | `CuentaUsuario` (`@OneToOne` + FK `miembro_id` única) | `LAZY` | ninguno | La cuenta pertenece al miembro y se crean juntas en el alta, pero se gestionan por separado. Solo la cuenta navega al miembro; `Miembro` no conoce su cuenta. |
| `Miembro` 1→N `Reserva` | `Reserva` (`@ManyToOne`, atributo `miembro`) | `LAZY` | ninguno | La reserva referencia a su miembro; RN-12 impide borrar un miembro con reservas. |
| `Sala` 1→N `Reserva` | `Reserva` (`@ManyToOne`, atributo `sala`) | `LAZY` | ninguno | Misma lógica que la anterior. |
| `Reserva` 1→N `DetalleReserva` | `Reserva` (`@OneToMany(mappedBy = "reserva")`) | `LAZY` | `cascade = ALL`, `orphanRemoval = true` | El detalle no existe sin la reserva: se guarda y se borra con ella. Quitar un servicio elimina su fila. |
| `DetalleReserva` N→1 `ServicioAdicional` | `DetalleReserva` (`@ManyToOne`, atributo `servicio`) | `LAZY` | ninguno | El detalle apunta al servicio; `precioUnitarioAplicado` conserva el precio histórico. |

Comprobaciones adicionales:

- `@Enumerated(EnumType.STRING)` en `TipoSala`, `EstadoMiembro`, `EstadoReserva` y `Rol`.
  Los **valores** siguen en inglés (`MEETING_ROOM`, `ACTIVE`, `PENDING`, `ADMIN`, etc.).
- `BigDecimal` para dinero, `LocalTime` para horarios de sede y `LocalDateTime` para reservas.
- Nombres de columna en inglés con `@Column(name = "…")`: `tarifaHora` → `hourly_rate`,
  `numeroDocumento` → `document_number`, `costoTotal` → `total_cost`, etc.
- `nullable = false` en los campos obligatorios y `unique` en número de documento, email,
  nombre de usuario y `(sede, nombre)` de sala.
- Colecciones `LAZY` y DTO de salida: el JSON nunca serializa entidades, así que no hay ciclos ni `LazyInitializationException`.
- `Miembro` y los listados de `Reserva` usan `@EntityGraph` para evitar N+1; los demás
  listados se resolverán con proyecciones o DTO en la fase 3.

---

## 4. Matriz de trazabilidad

### 4.1 Requisitos funcionales → reglas → endpoints

| RF | Reglas | Endpoints | Pantalla web |
|---|---|---|---|
| RF-01 | RN-11, RN-12, RN-13 | `/api/sedes`, `/api/salas`, `/api/equipamientos`, `/api/servicios-adicionales`, `/api/planes` | Catálogos (ADMIN) |
| RF-02 | RN-05, RN-11, RN-12, RN-13 | `/api/miembros` y subrutas | Miembros (ADMIN, RECEPTIONIST) |
| RF-03 | RN-01, RN-03, RN-04 | `GET /api/disponibilidad` | Disponibilidad |
| RF-04 | RN-01 … RN-07 | `POST /api/reservas` | Nueva reserva |
| RF-05 | RN-13 | `GET /api/reservas/{id}`, `GET /api/reservas?…` | Mis reservas / Reservas |
| RF-06 | RN-10, RN-07 | `POST` y `DELETE /api/reservas/{id}/servicios…` | Detalle de reserva |
| RF-07 | RN-09 | `POST /api/reservas/{id}/confirmar` | Detalle de reserva |
| RF-08 | RN-08, RN-09 | `POST /api/reservas/{id}/cancelar` | Detalle de reserva |
| RF-09 | RN-09 | `POST /api/reservas/{id}/completar` | Detalle de reserva |
| RF-10 | RN-07, RN-08 | `GET /api/miembros/{id}/consumo` | Mi consumo |
| RF-11 | RN-13 | `POST /auth/login` | Login |
| RF-12 | RN-13 | Filtro JWT + `@PreAuthorize` en todos los endpoints | Sesión y menú por rol |
| RF-13 | — | Swagger UI y `/v3/api-docs` | — |
| RF-14 | — | `GET /auth/perfil` | Cabecera de sesión |
| RF-15 | — | `GET /actuator/health` | — |

### 4.2 Escenarios de aceptación → verificación

| # | Escenario | Resultado esperado | Cubre |
|---|---|---|---|
| 1 | Reserva de sala libre con 2 servicios | `201` + costo calculado | RF-04, RN-07 |
| 2 | Reserva que se cruza con otra | `409` `RN-01` | RF-04, RN-01 |
| 3 | Reserva 10:00–11:00 tras otra 09:00–10:00 | `201` | RN-01 (extremos) |
| 4 | Reserva de 45 min o fuera de horario | `409` | RN-02 / RN-03 |
| 5 | Cancelación con 30 h | `200`, cargo `0` | RF-08, RN-08 |
| 6 | Cancelación con 5 h | `200`, cargo 50 % de la sala | RF-08, RN-08 |
| 7 | Cancelación con 1 h | `409` | RF-08, RN-08 |
| 8 | Confirmar una reserva cancelada | `409` | RN-09 |
| 9 | Miembro consulta reserva ajena | `403` | RN-13 |
| 10 | Endpoint protegido sin token / token alterado | `401` | RF-12 |
| 11 | Eliminar sala con reservas futuras | `409` | RN-12 |
| 12 | Crear sede sin nombre | `400` | Validación |
| 13 | Dos solicitudes simultáneas, misma sala y horario | Una `201`, otra `409` | RN-01 (concurrencia) |
| 14 | `GET /api/miembros?estado=SUSPENDED` | `200` solo suspendidos | RF-02 |

Los 14 escenarios tienen una prueba automatizada en
`src/test/java/com/coworkhub/aceptacion/EscenariosAceptacionTest.java`. La colección de
Postman en `docs/postman` sirve para probar manualmente las rutas principales; la prueba
de concurrencia se ejecuta en la suite automatizada porque necesita dos solicitudes a la
vez.

---

## 5. Supuestos y decisiones

1. **"Ahora" se inyecta.** Todo cálculo temporal usa un `Clock` configurable, para poder
   probar RN-03 y RN-08 con fechas fijas.
2. **Límites de cancelación.** Con exactamente 24 h se cancela **sin cargo**; con
   exactamente 2 h se cobra el **50 %**; con menos de 2 h se rechaza.
3. **Horas al cancelar tarde.** Con 24 h o más se devuelven las horas al plan. Entre 24 h
   y 2 h la reserva se cancela y se cobra el 50 % de la sala, pero **las horas quedan
   consumidas** (la sala estuvo bloqueada hasta la cancelación). Con menos de 2 h no hay
   cancelación.
4. **Qué reservas consumen horas.** Las `PENDING`, `CONFIRMED` y `COMPLETED` del mes de
   `inicio`; las `CANCELLED` no consumen salvo el caso del punto 3.
5. **Orden de consumo.** Las horas incluidas se consumen en orden cronológico de las
   reservas del mes; el excedente se calcula con la tarifa de la sala de cada reserva.
6. **Horas de una reserva.** Se cobran por duración exacta (múltiplos de 30 min); las
   horas incluidas se descuentan en horas decimales (45 min = 0,75 h).
7. **`costoTotal` al cancelar.** Si hay cargo, `costoTotal` pasa a ser el `cargoCancelacion`;
   `costoSala` y `costoServicios` se conservan como histórico. Sin cargo, `costoTotal = 0`.
8. **`maxReservasActivas`.** Cuenta reservas `PENDING` o `CONFIRMED` con `inicio` futuro.
9. **Servicios.** El precio se congela en `precioUnitarioAplicado` al agregarlos. Cambiar el
   precio del catálogo no altera reservas existentes. Solo se agregan/quitan en `PENDING`;
   agregar el mismo servicio dos veces suma cantidades.
10. **Estado inicial.** Toda reserva nace `PENDING`. Solo `ADMIN`/`RECEPTIONIST` confirman
    y completan.
11. **Unicidad de email.** Se normaliza a minúsculas al guardar y comparar (RN-11).
12. **Concurrencia RN-01.** La creación de reserva bloquea la fila de la sala
    (`PESSIMISTIC_WRITE`) dentro de la transacción, de modo que dos solicitudes
    simultáneas se serializan; la segunda vuelve a validar y recibe `409`.
13. **Zona horaria.** Las horas de negocio son `LocalDateTime` en la zona del servidor
    (configurable); los `timestamp` de error se emiten en UTC ISO-8601.
14. **Frontend.** Aplicación de una sola página con JavaScript vanilla (sin build), servida
    como estático por Spring Boot, que consume la API con `fetch` y guarda el JWT solo en
    memoria/`sessionStorage`.
15. **Nomenclatura.** Clases, atributos, métodos y variables en español; tablas y columnas
    en inglés mediante `@Table`/`@Column` (sección 3.1). Es una desviación deliberada del
    enunciado §2, que pedía atributos en inglés, para cumplir la rúbrica §4. El renombrado
    del código de la fase 2 se aplica como corrección 2.1 antes de iniciar la fase 3.

---

## 6. Fuera de alcance

- Pagos reales, facturación y pasarela de pago (los cargos son datos de la reserva).
- Notificaciones por email o calendario.
- Recuperación de contraseña, registro público y OAuth.
- Multi-tenant, multi-sucursal por usuario y auditoría histórica de cambios.
- Aplicación móvil nativa; la interfaz web es responsive pero no una app instalable.
- Reservas recurrentes y lista de espera.
