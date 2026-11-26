package com.coworkhub.negocio.implementacion;

import com.coworkhub.negocio.excepcion.RecursoNoEncontradoException;
import com.coworkhub.negocio.excepcion.ReglaNegocioException;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.negocio.servicio.ServicioServiciosAdicionales;
import com.coworkhub.persistencia.repositorio.RepositorioDetallesReserva;
import com.coworkhub.persistencia.repositorio.RepositorioServiciosAdicionales;
import com.coworkhub.presentacion.dto.RespuestaServicioAdicional;
import com.coworkhub.presentacion.dto.SolicitudServicioAdicional;
import com.coworkhub.presentacion.mapper.Mapeador;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.coworkhub.negocio.servicio.Validaciones.montoPositivo;
import static com.coworkhub.negocio.servicio.Validaciones.texto;

@Service
@Transactional(readOnly = true)
public class ServicioServiciosAdicionalesImpl implements ServicioServiciosAdicionales {

    private final RepositorioServiciosAdicionales repositorioServicios;
    private final RepositorioDetallesReserva repositorioDetalles;
    private final Mapeador mapeador;

    public ServicioServiciosAdicionalesImpl(RepositorioServiciosAdicionales repositorioServicios,
                                            RepositorioDetallesReserva repositorioDetalles, Mapeador mapeador) {
        this.repositorioServicios = repositorioServicios;
        this.repositorioDetalles = repositorioDetalles;
        this.mapeador = mapeador;
    }

    @Override
    public List<RespuestaServicioAdicional> listar() {
        return repositorioServicios.findAllByOrderByNombreAsc().stream()
                .map(mapeador::aRespuesta)
                .toList();
    }

    @Override
    public RespuestaServicioAdicional buscar(Long id) {
        return mapeador.aRespuesta(obtener(id));
    }

    @Override
    @Transactional
    public RespuestaServicioAdicional crear(SolicitudServicioAdicional solicitud) {
        String nombre = texto(solicitud.nombre(), "nombre");
        montoPositivo(solicitud.precioUnitario(), "precioUnitario");
        if (repositorioServicios.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un servicio adicional con ese nombre");
        }
        ServicioAdicional servicio = new ServicioAdicional(nombre, solicitud.precioUnitario());
        return mapeador.aRespuesta(repositorioServicios.save(servicio));
    }

    @Override
    @Transactional
    public RespuestaServicioAdicional actualizar(Long id, SolicitudServicioAdicional solicitud) {
        ServicioAdicional servicio = obtener(id);
        String nombre = texto(solicitud.nombre(), "nombre");
        montoPositivo(solicitud.precioUnitario(), "precioUnitario");
        if (repositorioServicios.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("RN-11", "Ya existe un servicio adicional con ese nombre");
        }
        servicio.setNombre(nombre);
        servicio.setPrecioUnitario(solicitud.precioUnitario());
        return mapeador.aRespuesta(repositorioServicios.save(servicio));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        ServicioAdicional servicio = obtener(id);
        if (repositorioDetalles.existsByServicioId(id)) {
            throw new ReglaNegocioException("RN-12",
                    "No se puede eliminar el servicio: se uso en una reserva");
        }
        repositorioServicios.delete(servicio);
    }

    private ServicioAdicional obtener(Long id) {
        return repositorioServicios.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio adicional", id));
    }
}
