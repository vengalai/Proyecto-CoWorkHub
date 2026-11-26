package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.excepcion.SolicitudInvalidaException;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.negocio.servicio.ServicioSedes;
import com.coworkhub.persistencia.repositorio.RepositorioSalas;
import com.coworkhub.persistencia.repositorio.RepositorioSedes;
import com.coworkhub.presentacion.dto.RespuestaSede;
import com.coworkhub.presentacion.dto.SolicitudSede;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.coworkhub.negocio.servicio.Validaciones.texto;

@Service
@Transactional(readOnly = true)
public class ServicioSedesImpl implements ServicioSedes {

    private final RepositorioSedes repositorioSedes;
    private final RepositorioSalas repositorioSalas;
    private final Mapeador mapeador;

    public ServicioSedesImpl(RepositorioSedes repositorioSedes, RepositorioSalas repositorioSalas,
                             Mapeador mapeador) {
        this.repositorioSedes = repositorioSedes;
        this.repositorioSalas = repositorioSalas;
        this.mapeador = mapeador;
    }

    @Override
    public List<RespuestaSede> listar() {
        return repositorioSedes.findAllByOrderByNombreAsc().stream()
                .map(mapeador::aRespuesta)
                .toList();
    }

    @Override
    public RespuestaSede buscar(Long id) {
        return mapeador.aRespuesta(obtener(id));
    }

    @Override
    @Transactional
    public RespuestaSede crear(SolicitudSede solicitud) {
        validarHorario(solicitud);
        Sede sede = new Sede(texto(solicitud.nombre(), "nombre"), texto(solicitud.ciudad(), "ciudad"),
                texto(solicitud.direccion(), "direccion"), solicitud.horaApertura(), solicitud.horaCierre());
        return mapeador.aRespuesta(repositorioSedes.save(sede));
    }

    @Override
    @Transactional
    public RespuestaSede actualizar(Long id, SolicitudSede solicitud) {
        validarHorario(solicitud);
        Sede sede = obtener(id);
        sede.setNombre(texto(solicitud.nombre(), "nombre"));
        sede.setCiudad(texto(solicitud.ciudad(), "ciudad"));
        sede.setDireccion(texto(solicitud.direccion(), "direccion"));
        sede.setHoraApertura(solicitud.horaApertura());
        sede.setHoraCierre(solicitud.horaCierre());
        return mapeador.aRespuesta(repositorioSedes.save(sede));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Sede sede = obtener(id);
        if (repositorioSalas.existsBySedeId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar la sede: tiene salas asociadas");
        }
        repositorioSedes.delete(sede);
    }

    private Sede obtener(Long id) {
        return repositorioSedes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Sede", id));
    }

    private void validarHorario(SolicitudSede solicitud) {
        if (solicitud.horaApertura() == null || solicitud.horaCierre() == null) {
            throw new SolicitudInvalidaException("Las horas de apertura y cierre son obligatorias");
        }
        if (!solicitud.horaApertura().isBefore(solicitud.horaCierre())) {
            throw new SolicitudInvalidaException("El campo 'horaApertura' debe ser anterior a 'horaCierre'");
        }
    }
}
