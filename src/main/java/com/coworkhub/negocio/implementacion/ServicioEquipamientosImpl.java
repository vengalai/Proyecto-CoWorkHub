package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.servicio.ServicioEquipamientos;
import com.coworkhub.persistencia.repositorio.RepositorioEquipamientos;
import com.coworkhub.persistencia.repositorio.RepositorioSalas;
import com.coworkhub.presentacion.dto.RespuestaEquipamiento;
import com.coworkhub.presentacion.dto.SolicitudEquipamiento;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.coworkhub.negocio.servicio.Validaciones.texto;

@Service
@Transactional(readOnly = true)
public class ServicioEquipamientosImpl implements ServicioEquipamientos {

    private final RepositorioEquipamientos repositorioEquipamientos;
    private final RepositorioSalas repositorioSalas;
    private final Mapeador mapeador;

    public ServicioEquipamientosImpl(RepositorioEquipamientos repositorioEquipamientos,
                                     RepositorioSalas repositorioSalas, Mapeador mapeador) {
        this.repositorioEquipamientos = repositorioEquipamientos;
        this.repositorioSalas = repositorioSalas;
        this.mapeador = mapeador;
    }

    @Override
    public List<RespuestaEquipamiento> listar() {
        return repositorioEquipamientos.findAllByOrderByNombreAsc().stream()
                .map(mapeador::aRespuesta)
                .toList();
    }

    @Override
    public RespuestaEquipamiento buscar(Long id) {
        return mapeador.aRespuesta(obtener(id));
    }

    @Override
    @Transactional
    public RespuestaEquipamiento crear(SolicitudEquipamiento solicitud) {
        String nombre = texto(solicitud.nombre(), "nombre");
        if (repositorioEquipamientos.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un equipamiento con ese nombre");
        }
        return mapeador.aRespuesta(repositorioEquipamientos.save(new Equipamiento(nombre)));
    }

    @Override
    @Transactional
    public RespuestaEquipamiento actualizar(Long id, SolicitudEquipamiento solicitud) {
        Equipamiento equipamiento = obtener(id);
        String nombre = texto(solicitud.nombre(), "nombre");
        if (repositorioEquipamientos.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un equipamiento con ese nombre");
        }
        equipamiento.setNombre(nombre);
        return mapeador.aRespuesta(repositorioEquipamientos.save(equipamiento));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Equipamiento equipamiento = obtener(id);
        if (repositorioSalas.existsByEquipamientosId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar el equipamiento: esta asignado a una sala");
        }
        repositorioEquipamientos.delete(equipamiento);
    }

    private Equipamiento obtener(Long id) {
        return repositorioEquipamientos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Equipamiento", id));
    }
}
