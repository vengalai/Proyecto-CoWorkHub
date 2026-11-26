package com.coworkhub.negocio.servicio;

import com.coworkhub.negocio.model.DetalleReserva;
import com.coworkhub.negocio.model.PlanMembresia;
import com.coworkhub.negocio.model.Sala;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculos de dinero de RN-07 y RN-08. Se aislan en un bean propio para que
 * el servicio de reservas quede mas simple y las cuentas se puedan probar aparte.
 */
@Component
public class CalculadoraCostoReserva {

    public record CostoSala(BigDecimal costo, long minutosIncluidos, long minutosExcedentes) {
    }

    public CostoSala calcularCostoSala(Sala sala, PlanMembresia plan, long minutosReserva,
                                       long minutosYaConsumidos) {
        long minutosIncluidosPlan = plan.getHorasIncluidasMes() * 60L;
        long minutosDisponibles = Math.max(0L, minutosIncluidosPlan - minutosYaConsumidos);
        long minutosIncluidos = Math.min(minutosReserva, minutosDisponibles);
        long minutosExcedentes = minutosReserva - minutosIncluidos;

        BigDecimal horasExcedentes = BigDecimal.valueOf(minutosExcedentes)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        BigDecimal factorDescuento = BigDecimal.valueOf(100 - plan.getDescuentoExcedente())
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        BigDecimal costo = horasExcedentes
                .multiply(sala.getTarifaPorHora())
                .multiply(factorDescuento)
                .setScale(2, RoundingMode.HALF_UP);
        return new CostoSala(costo, minutosIncluidos, minutosExcedentes);
    }

    public BigDecimal calcularCostoServicios(List<DetalleReserva> detalles) {
        return detalles.stream()
                .map(DetalleReserva::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularCargoCancelacion(BigDecimal costoSala) {
        return costoSala.multiply(BigDecimal.valueOf(50))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
