package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.negocio.servicio.ServicioSalas;
import com.coworkhub.persistencia.repositorio.RepositorioEquipamientos;
import com.coworkhub.persistencia.repositorio.RepositorioReservas;
import com.coworkhub.persistencia.repositorio.RepositorioSalas;
import com.coworkhub.persistencia.repositorio.RepositorioSedes;
import com.coworkhub.presentacion.dto.RespuestaSala;
import com.coworkhub.presentacion.dto.SolicitudSala;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coworkhub.negocio.servicio.Validaciones.texto;

@Service
@Transactional(readOnly = true)
public class ServicioSalasImpl implements ServicioSalas {

    private final RepositorioSalas repositorioSalas;
    private final RepositorioSedes repositorioSedes;
    private final RepositorioEquipamientos repositorioEquipamientos;
    private final RepositorioReservas repositorioReservas;
    private final Mapeador mapeador;

    public ServicioSalasImpl(RepositorioSalas repositorioSalas, RepositorioSedes repositorioSedes,
                             RepositorioEquipamientos repositorioEquipamientos,
                             RepositorioReservas repositorioReservas, Mapeador mapeador) {
        this.repositorioSalas = repositorioSalas;
        this.repositorioSedes = repositorioSedes;
        this.repositorioEquipamientos = repositorioEquipamientos;
        this.repositorioReservas = repositorioReservas;
        this.mapeador = mapeador;
    }

    @Override
    public List<RespuestaSala> listar(Long sedeId) {
        List<Sala> salas;
        if (sedeId == null) {
            salas = repositorioSalas.findAllByOrderByNombreAsc();
        } else {
            if (!repositorioSedes.existsById(sedeId)) {
                throw new RecursoNoEncontradoException("Sede", sedeId);
            }
            salas = repositorioSalas.findBySedeIdOrderByNombreAsc(sedeId);
        }
        return salas.stream().map(mapeador::aRespuesta).toList();
    }

    @Override
    public RespuestaSala buscar(Long id) {
        return mapeador.aRespuesta(obtenerCompleta(id));
    }

    @Override
    @Transactional
    public RespuestaSala crear(SolicitudSala solicitud) {
        Sede sede = obtenerSede(solicitud.sedeId());
        validarNombre(sede.getId(), texto(solicitud.nombre(), "nombre"), null);
        Sala sala = new Sala(sede, solicitud.nombre().trim(), solicitud.tipo(), solicitud.capacidad(),
                solicitud.tarifaPorHora(), solicitud.activa() == null || solicitud.activa());
        sala.getEquipamientos().addAll(cargarEquipamientos(solicitud.equipamientoIds()));
        return mapeador.aRespuesta(repositorioSalas.save(sala));
    }

    @Override
    @Transactional
    public RespuestaSala actualizar(Long id, SolicitudSala solicitud) {
        Sala sala = obtenerCompleta(id);
        Sede sede = obtenerSede(solicitud.sedeId());
        validarNombre(sede.getId(), texto(solicitud.nombre(), "nombre"), id);
        sala.setSede(sede);
        sala.setNombre(solicitud.nombre().trim());
        sala.setTipo(solicitud.tipo());
        sala.setCapacidad(solicitud.capacidad());
        sala.setTarifaPorHora(solicitud.tarifaPorHora());
        sala.setActiva(solicitud.activa() == null || solicitud.activa());
        sala.getEquipamientos().clear();
        sala.getEquipamientos().addAll(cargarEquipamientos(solicitud.equipamientoIds()));
        return mapeador.aRespuesta(repositorioSalas.save(sala));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Sala sala = obtenerCompleta(id);
        if (repositorioReservas.existsBySalaId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar la sala: tiene reservas registradas (activas o historicas)");
        }
        repositorioSalas.delete(sala);
    }

    private Sala obtenerCompleta(Long id) {
        return repositorioSalas.buscarCompleta(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sala", id));
    }

    private Sede obtenerSede(Long sedeId) {
        return repositorioSedes.findById(sedeId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", sedeId));
    }

    private void validarNombre(Long sedeId, String nombre, Long idActual) {
        boolean existe = idActual == null
                ? repositorioSalas.existsBySedeIdAndNombreIgnoreCase(sedeId, nombre)
                : repositorioSalas.existsBySedeIdAndNombreIgnoreCaseAndIdNot(sedeId, nombre, idActual);
        if (existe) {
            throw new ReglaNegocioException("RN-11", "Ya existe una sala con ese nombre en la sede");
        }
    }

    private Set<Equipamiento> cargarEquipamientos(List<Long> ids) {
        Set<Equipamiento> equipamientos = new HashSet<>();
        if (ids == null) {
            return equipamientos;
        }
        for (Long id : new HashSet<>(ids)) {
            equipamientos.add(repositorioEquipamientos.findById(id)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id)));
        }
        return equipamientos;
    }
}
