package com.coworkhub.negocio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "booking_items")
public class DetalleReserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServicioAdicional servicio;

    @Column(name = "quantity", nullable = false)
    private int cantidad;

    @Column(name = "applied_unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioAplicado;

    protected DetalleReserva() {
        // Requerido por JPA.
    }

    public DetalleReserva(ServicioAdicional servicio, int cantidad, BigDecimal precioUnitarioAplicado) {
        this.servicio = servicio;
        this.cantidad = cantidad;
        this.precioUnitarioAplicado = precioUnitarioAplicado;
    }

    public BigDecimal getSubtotal() {
        return precioUnitarioAplicado.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getId() {
        return id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public ServicioAdicional getServicio() {
        return servicio;
    }

    public void setServicio(ServicioAdicional servicio) {
        this.servicio = servicio;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitarioAplicado() {
        return precioUnitarioAplicado;
    }

    public void setPrecioUnitarioAplicado(BigDecimal precioUnitarioAplicado) {
        this.precioUnitarioAplicado = precioUnitarioAplicado;
    }
}
