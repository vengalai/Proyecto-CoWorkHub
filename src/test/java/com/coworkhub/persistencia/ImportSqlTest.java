package com.coworkhub.persistencia;

import com.coworkhub.negocio.model.EstadoMiembro;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.Reserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.persistencia.repositorio.RepositorioCuentasUsuario;
import com.coworkhub.persistencia.repositorio.RepositorioDetallesReserva;
import com.coworkhub.persistencia.repositorio.RepositorioEquipamientos;
import com.coworkhub.persistencia.repositorio.RepositorioMiembros;
import com.coworkhub.persistencia.repositorio.RepositorioPlanes;
import com.coworkhub.persistencia.repositorio.RepositorioReservas;
import com.coworkhub.persistencia.repositorio.RepositorioSalas;
import com.coworkhub.persistencia.repositorio.RepositorioSedes;
import com.coworkhub.persistencia.repositorio.RepositorioServiciosAdicionales;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ImportSqlTest {

    @Autowired
    private RepositorioSedes repositorioSedes;

    @Autowired
    private RepositorioSalas repositorioSalas;

    @Autowired
    private RepositorioEquipamientos repositorioEquipamientos;

    @Autowired
    private RepositorioPlanes repositorioPlanes;

    @Autowired
    private RepositorioMiembros repositorioMiembros;

    @Autowired
    private RepositorioCuentasUsuario repositorioCuentas;

    @Autowired
    private RepositorioServiciosAdicionales repositorioServicios;

    @Autowired
    private RepositorioReservas repositorioReservas;

    @Autowired
    private RepositorioDetallesReserva repositorioDetalles;

    @Test
    void cargaLosMinimosExigidosPorElEnunciado() {
        assertThat(repositorioSedes.count()).isEqualTo(2);
        assertThat(repositorioSalas.count()).isEqualTo(6);
        assertThat(repositorioEquipamientos.count()).isEqualTo(4);
        assertThat(repositorioServicios.count()).isEqualTo(3);
        assertThat(repositorioPlanes.count()).isEqualTo(3);
        assertThat(repositorioMiembros.count()).isEqualTo(5);
        assertThat(repositorioCuentas.count()).isEqualTo(4);
        assertThat(repositorioReservas.count()).isEqualTo(15);
        assertThat(repositorioDetalles.count()).isEqualTo(9);
    }

    @Test
    void hayUnaSalaInactivaYUnMiembroSuspendido() {
        List<Sala> salas = repositorioSalas.findAll();
        assertThat(salas.stream().filter(sala -> !sala.isActiva())).hasSize(1);

        List<Miembro> suspendidos = repositorioMiembros.findByEstadoOrderByNombreCompletoAsc(EstadoMiembro.SUSPENDED);
        assertThat(suspendidos).hasSize(1);
        assertThat(suspendidos.get(0).getNombreCompleto()).isEqualTo("Elena Ruiz");
    }

    @Test
    void lasRelacionesCargaronSusFilas() {
        Sala salaAurora = repositorioSalas.buscarCompleta(1L).orElseThrow();
        assertThat(salaAurora.getEquipamientos()).hasSize(2);

        Reserva reservaConDosServicios = repositorioReservas.buscarCompleta(1L).orElseThrow();
        assertThat(reservaConDosServicios.getDetalles()).hasSize(2);
        assertThat(reservaConDosServicios.getCostoTotal()).isEqualByComparingTo("150.00");
        assertThat(reservaConDosServicios.getMinutosConsumidos()).isEqualTo(120);
    }
}
