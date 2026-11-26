package com.coworkhub.aceptacion;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de aceptacion para los 14 escenarios definidos en docs/01-analisis.md. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(EscenariosAceptacionTest.RelojFijo.class)
class EscenariosAceptacionTest {

    private static final ZoneId ZONA = ZoneId.of("America/Argentina/Buenos_Aires");
    private static final LocalDate HOY = LocalDate.now(ZONA);
    private static final Pattern PATRON_TOKEN = Pattern.compile("\\\"token\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern PATRON_ID = Pattern.compile("\\\"id\\\"\\s*:\\s*(\\d+)");

    @Value("${local.server.port}")
    private int puerto;

    private final HttpClient cliente = HttpClient.newHttpClient();
    private String tokenAdmin;

    @Autowired
    private JdbcTemplate jdbc;

    @TestConfiguration
    static class RelojFijo {
        @Bean
        @Primary
        Clock relojDePruebas() {
            return Clock.fixed(HOY.atTime(8, 0).atZone(ZONA).toInstant(), ZONA);
        }
    }

    @org.junit.jupiter.api.BeforeEach
    void iniciarSesionComoAdministrador() {
        tokenAdmin = iniciarSesion("admin");
    }

    @Test
    @Order(1)
    void escenario01_creaReservaConDosServicios() {
        Respuesta respuesta = solicitar("POST", "/api/reservas",
                reserva(1, 1, HOY.plusDays(10), "10:00:00", "12:00:00", 4,
                        "[{\"servicioId\":1,\"cantidad\":1},{\"servicioId\":2,\"cantidad\":1}]"), tokenAdmin);
        assertEquals(201, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("\"costoServicios\":80.00"));
    }

    @Test
    @Order(2)
    void escenario02_rechazaUnaReservaQueSeCruza() {
        Respuesta respuesta = solicitar("POST", "/api/reservas",
                reserva(3, 1, HOY.plusDays(10), "11:00:00", "12:00:00", 2, "[]"), tokenAdmin);
        assertEquals(409, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("RN-01"));
    }

    @Test
    @Order(3)
    void escenario03_permiteReservasQueSoloSeTocanEnElExtremo() {
        Respuesta respuesta = solicitar("POST", "/api/reservas",
                reserva(3, 1, HOY.plusDays(10), "12:00:00", "13:00:00", 2, "[]"), tokenAdmin);
        assertEquals(201, respuesta.estado());
    }

    @Test
    @Order(4)
    void escenario04_rechazaDuracionInvalidaYHorarioFueraDeLaSede() {
        Respuesta duracion = solicitar("POST", "/api/reservas",
                reserva(2, 2, HOY.plusDays(11), "10:00:00", "10:45:00", 2, "[]"), tokenAdmin);
        Respuesta horario = solicitar("POST", "/api/reservas",
                reserva(2, 2, HOY.plusDays(11), "21:00:00", "22:00:00", 2, "[]"), tokenAdmin);
        assertEquals(409, duracion.estado());
        assertEquals(409, horario.estado());
    }

    @Test
    @Order(5)
    void escenario05_cancelaConMasDe24HorasSinCargo() {
        String id = crearReserva(2, 2, HOY.plusDays(1), "10:00:00", "11:00:00");
        Respuesta respuesta = solicitar("POST", "/api/reservas/" + id + "/cancelar", "", tokenAdmin);
        assertEquals(200, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("\"cargoCancelacion\":0.00"));
    }

    @Test
    @Order(6)
    void escenario06_cancelaConCincoHorasYAplicaLaMitadDelCostoDeLaSala() {
        // El plan de prueba queda sin horas incluidas para comprobar el cargo del 50 %.
        jdbc.update("UPDATE membership_plans SET included_hours_per_month = 0 WHERE id = 1");
        String id = crearReserva(2, 2, HOY, "13:00:00", "14:00:00");
        Respuesta respuesta = solicitar("POST", "/api/reservas/" + id + "/cancelar", "", tokenAdmin);
        assertEquals(200, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("\"cargoCancelacion\":22.50"));
    }

    @Test
    @Order(7)
    void escenario07_noPermiteCancelarConMenosDeDosHoras() {
        String id = crearReserva(2, 2, HOY, "09:30:00", "10:30:00");
        Respuesta respuesta = solicitar("POST", "/api/reservas/" + id + "/cancelar", "", tokenAdmin);
        assertEquals(409, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("RN-08"));
    }

    @Test
    @Order(8)
    void escenario08_noPermiteConfirmarUnaReservaCancelada() {
        String id = crearReserva(3, 2, HOY.plusDays(12), "10:00:00", "11:00:00");
        assertEquals(200, solicitar("POST", "/api/reservas/" + id + "/cancelar", "", tokenAdmin).estado());
        Respuesta respuesta = solicitar("POST", "/api/reservas/" + id + "/confirmar", "", tokenAdmin);
        assertEquals(409, respuesta.estado());
    }

    @Test
    @Order(9)
    void escenario09_unMiembroNoPuedeConsultarLaReservaDeOtro() {
        String tokenAna = iniciarSesion("ana");
        Respuesta respuesta = solicitar("GET", "/api/reservas/2", "", tokenAna);
        assertEquals(403, respuesta.estado());
    }

    @Test
    @Order(10)
    void escenario10_requiereUnTokenValido() {
        assertEquals(401, solicitar("GET", "/api/sedes", "", null).estado());
        assertEquals(401, solicitar("GET", "/api/sedes", "", "token.alterado.invalido").estado());
    }

    @Test
    @Order(11)
    void escenario11_noEliminaUnaSalaConReservasAsociadas() {
        Respuesta respuesta = solicitar("DELETE", "/api/salas/1", "", tokenAdmin);
        assertEquals(409, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("RN-12"));
    }

    @Test
    @Order(12)
    void escenario12_rechazaUnaSedeSinNombre() {
        String cuerpo = "{\"nombre\":\"\",\"ciudad\":\"Buenos Aires\","
                + "\"direccion\":\"Calle 123\",\"horaApertura\":\"08:00\",\"horaCierre\":\"20:00\"}";
        Respuesta respuesta = solicitar("POST", "/api/sedes", cuerpo, tokenAdmin);
        assertEquals(400, respuesta.estado());
    }

    @Test
    @Order(13)
    void escenario13_soloUnaDeDosReservasSimultaneasSeGuarda() {
        LocalDate fecha = HOY.plusDays(13);
        String solicitudUno = reserva(1, 2, fecha, "10:00:00", "11:00:00", 2, "[]");
        String solicitudDos = reserva(3, 2, fecha, "10:00:00", "11:00:00", 2, "[]");
        CompletableFuture<Respuesta> uno = CompletableFuture.supplyAsync(
                () -> solicitar("POST", "/api/reservas", solicitudUno, tokenAdmin));
        CompletableFuture<Respuesta> dos = CompletableFuture.supplyAsync(
                () -> solicitar("POST", "/api/reservas", solicitudDos, tokenAdmin));
        CompletableFuture.allOf(uno, dos).join();
        int estadoUno = uno.join().estado();
        int estadoDos = dos.join().estado();
        assertTrue((estadoUno == 201 && estadoDos == 409) || (estadoUno == 409 && estadoDos == 201),
                "Se esperaba una respuesta 201 y otra 409, pero llegaron " + estadoUno + " y " + estadoDos);
    }

    @Test
    @Order(14)
    void escenario14_filtraMiembrosSuspendidos() {
        Respuesta respuesta = solicitar("GET", "/api/miembros?estado=SUSPENDED&page=0&size=20", "", tokenAdmin);
        assertEquals(200, respuesta.estado());
        assertTrue(respuesta.cuerpo().contains("Elena Ruiz"));
        assertTrue(respuesta.cuerpo().contains("SUSPENDED"));
    }

    private String crearReserva(long miembroId, long salaId, LocalDate fecha, String inicio, String fin) {
        Respuesta respuesta = solicitar("POST", "/api/reservas",
                reserva(miembroId, salaId, fecha, inicio, fin, 2, "[]"), tokenAdmin);
        assertEquals(201, respuesta.estado(), respuesta.cuerpo());
        Matcher coincidencia = PATRON_ID.matcher(respuesta.cuerpo());
        assertTrue(coincidencia.find(), "La respuesta debe incluir el id de la reserva");
        return coincidencia.group(1);
    }

    private String iniciarSesion(String usuario) {
        Respuesta respuesta = solicitar("POST", "/auth/login",
                "{\"nombreUsuario\":\"" + usuario + "\",\"contrasena\":\"coworkhub123\"}", null);
        assertEquals(200, respuesta.estado(), respuesta.cuerpo());
        Matcher coincidencia = PATRON_TOKEN.matcher(respuesta.cuerpo());
        assertTrue(coincidencia.find(), "La respuesta de inicio de sesion debe traer un token");
        return coincidencia.group(1);
    }

    private String reserva(long miembroId, long salaId, LocalDate fecha,
                           String inicio, String fin, int asistentes, String servicios) {
        return "{\"miembroId\":" + miembroId + ",\"salaId\":" + salaId
                + ",\"inicio\":\"" + fecha + "T" + inicio + "\",\"fin\":\"" + fecha + "T" + fin
                + "\",\"asistentes\":" + asistentes + ",\"servicios\":" + servicios + "}";
    }

    private Respuesta solicitar(String metodo, String ruta, String cuerpo, String token) {
        HttpRequest.Builder solicitud = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + ruta))
                .header("Accept", "application/json");
        if (token != null) {
            solicitud.header("Authorization", "Bearer " + token);
        }
        if (cuerpo == null || cuerpo.isEmpty()) {
            solicitud.method(metodo, HttpRequest.BodyPublishers.noBody());
        } else {
            solicitud.header("Content-Type", "application/json")
                    .method(metodo, HttpRequest.BodyPublishers.ofString(cuerpo));
        }
        try {
            HttpResponse<String> respuesta = cliente.send(solicitud.build(), HttpResponse.BodyHandlers.ofString());
            return new Respuesta(respuesta.statusCode(), respuesta.body());
        } catch (IOException excepcion) {
            throw new IllegalStateException("No se pudo llamar a la API de prueba", excepcion);
        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La solicitud de prueba fue interrumpida", excepcion);
        }
    }

    private record Respuesta(int estado, String cuerpo) { }
}
