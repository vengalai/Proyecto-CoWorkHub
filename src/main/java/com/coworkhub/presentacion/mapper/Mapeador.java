package com.coworkhub.presentacion.mapper;

import com.coworkhub.negocio.model.DetalleReserva;
import com.coworkhub.negocio.model.Equipamiento;
import com.coworkhub.negocio.model.Miembro;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.negocio.model.Reserva;
import com.coworkhub.negocio.model.Sala;
import com.coworkhub.negocio.model.Sede;
import com.coworkhub.negocio.model.ServicioAdicional;
import com.coworkhub.presentacion.dto.RespuestaDetalleReserva;
import com.coworkhub.presentacion.dto.RespuestaEquipamiento;
import com.coworkhub.presentacion.dto.RespuestaMiembro;
import com.coworkhub.presentacion.dto.RespuestaPlan;
import com.coworkhub.presentacion.dto.RespuestaReserva;
import com.coworkhub.presentacion.dto.RespuestaSala;
import com.coworkhub.presentacion.dto.RespuestaSede;
import com.coworkhub.presentacion.dto.RespuestaServicioAdicional;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

/**
 * Convierte entidades en DTO de salida. Ninguna entidad se serializa directamente.
 */
@Component
public class Mapeador {

    public RespuestaSede aRespuesta(Sede sede) {
        return new RespuestaSede(sede.getId(), sede.getNombre(), sede.getCiudad(), sede.getDireccion(),
                sede.getHoraApertura(), sede.getHoraCierre());
    }

    public RespuestaEquipamiento aRespuesta(Equipamiento equipamiento) {
        return new RespuestaEquipamiento(equipamiento.getId(), equipamiento.getNombre());
    }

    public RespuestaSala aRespuesta(Sala sala) {
        List<RespuestaEquipamiento> equipamientos = sala.getEquipamientos().stream()
                .sorted(Comparator.comparing(Equipamiento::getNombre))
                .map(this::aRespuesta)
                .toList();
        return new RespuestaSala(sala.getId(), sala.getNombre(), sala.getTipo(), sala.getCapacidad(),
                sala.getTarifaPorHora(), sala.isActiva(), sala.getSede().getId(), sala.getSede().getNombre(),
                equipamientos);
    }

    public RespuestaServicioAdicional aRespuesta(ServicioAdicional servicio) {
        return new RespuestaServicioAdicional(servicio.getId(), servicio.getNombre(), servicio.getPrecioUnitario());
    }

    public RespuestaPlan aRespuesta(PlanMembresia plan) {
        return new RespuestaPlan(plan.getId(), plan.getNombre(), plan.getHorasIncluidasMes(),
                plan.getDescuentoExcedente(), plan.getMaxReservasActivas());
    }

    public RespuestaMiembro aRespuesta(Miembro miembro) {
        String nombreUsuario = miembro.getCuenta() == null ? null : miembro.getCuenta().getNombreUsuario();
        return new RespuestaMiembro(miembro.getId(), miembro.getNumeroDocumento(), miembro.getNombreCompleto(),
                miembro.getEmail(), miembro.getEstado(), miembro.getPlan().getId(), miembro.getPlan().getNombre(),
                nombreUsuario);
    }

    public RespuestaDetalleReserva aRespuesta(DetalleReserva detalle) {
        return new RespuestaDetalleReserva(detalle.getServicio().getId(), detalle.getServicio().getNombre(),
                detalle.getCantidad(), detalle.getPrecioUnitarioAplicado(),
                detalle.getSubtotal().setScale(2, RoundingMode.HALF_UP));
    }

    public RespuestaReserva aRespuesta(Reserva reserva) {
        List<RespuestaDetalleReserva> detalles = reserva.getDetalles().stream()
                .sorted(Comparator.comparing(detalle -> detalle.getServicio().getId()))
                .map(this::aRespuesta)
                .toList();
        return new RespuestaReserva(reserva.getId(), reserva.getMiembro().getId(),
                reserva.getMiembro().getNombreCompleto(), reserva.getSala().getId(), reserva.getSala().getNombre(),
                reserva.getSala().getSede().getNombre(), reserva.getInicio(), reserva.getFin(),
                reserva.getAsistentes(), reserva.getEstado(), reserva.getCostoSala(),
                reserva.getCostoServicios(), reserva.getCostoTotal(), reserva.getCargoCancelacion(), detalles);
    }
}
