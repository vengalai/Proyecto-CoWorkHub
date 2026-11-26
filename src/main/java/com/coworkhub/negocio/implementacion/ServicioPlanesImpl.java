package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.negocio.servicio.ServicioPlanes;
import com.coworkhub.persistencia.repositorio.RepositorioMiembros;
import com.coworkhub.persistencia.repositorio.RepositorioPlanes;
import com.coworkhub.presentacion.dto.RespuestaPlan;
import com.coworkhub.presentacion.dto.SolicitudPlan;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.coworkhub.negocio.servicio.Validaciones.rango;
import static com.coworkhub.negocio.servicio.Validaciones.texto;

@Service
@Transactional(readOnly = true)
public class ServicioPlanesImpl implements ServicioPlanes {

    private final RepositorioPlanes repositorioPlanes;
    private final RepositorioMiembros repositorioMiembros;
    private final Mapeador mapeador;

    public ServicioPlanesImpl(RepositorioPlanes repositorioPlanes, RepositorioMiembros repositorioMiembros,
                              Mapeador mapeador) {
        this.repositorioPlanes = repositorioPlanes;
        this.repositorioMiembros = repositorioMiembros;
        this.mapeador = mapeador;
    }

    @Override
    public List<RespuestaPlan> listar() {
        return repositorioPlanes.findAllByOrderByNombreAsc().stream()
                .map(mapeador::aRespuesta)
                .toList();
    }

    @Override
    public RespuestaPlan buscar(Long id) {
        return mapeador.aRespuesta(obtener(id));
    }

    @Override
    @Transactional
    public RespuestaPlan crear(SolicitudPlan solicitud) {
        validar(solicitud);
        if (repositorioPlanes.existsByNombreIgnoreCase(solicitud.nombre().trim())) {
            throw new ReglaNegocioException("RN-11", "Ya existe un plan con ese nombre");
        }
        PlanMembresia plan = new PlanMembresia(solicitud.nombre().trim(), solicitud.horasIncluidasMes(),
                solicitud.descuentoExcedente(), solicitud.maxReservasActivas());
        return mapeador.aRespuesta(repositorioPlanes.save(plan));
    }

    @Override
    @Transactional
    public RespuestaPlan actualizar(Long id, SolicitudPlan solicitud) {
        PlanMembresia plan = obtener(id);
        validar(solicitud);
        if (repositorioPlanes.existsByNombreIgnoreCaseAndIdNot(solicitud.nombre().trim(), id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un plan con ese nombre");
        }
        plan.setNombre(solicitud.nombre().trim());
        plan.setHorasIncluidasMes(solicitud.horasIncluidasMes());
        plan.setDescuentoExcedente(solicitud.descuentoExcedente());
        plan.setMaxReservasActivas(solicitud.maxReservasActivas());
        return mapeador.aRespuesta(repositorioPlanes.save(plan));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        PlanMembresia plan = obtener(id);
        if (repositorioMiembros.existsByPlanId(id)) {
            throw new ReglaNegocioException("RN-12", "No se puede eliminar el plan: tiene miembros asociados");
        }
        repositorioPlanes.delete(plan);
    }

    private PlanMembresia obtener(Long id) {
        return repositorioPlanes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plan", id));
    }

    private void validar(SolicitudPlan solicitud) {
        texto(solicitud.nombre(), "nombre");
        if (solicitud.horasIncluidasMes() < 0) {
            throw new com.coworkhub.negocio.excepcion.SolicitudInvalidaException(
                    "El campo 'horasIncluidasMes' no puede ser negativo");
        }
        rango(solicitud.descuentoExcedente(), 0, 100, "descuentoExcedente");
        if (solicitud.maxReservasActivas() < 1) {
            throw new com.coworkhub.negocio.excepcion.SolicitudInvalidaException(
                    "El campo 'maxReservasActivas' debe ser mayor que cero");
        }
    }
}
