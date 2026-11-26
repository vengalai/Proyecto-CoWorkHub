package com.coworkhub.negocio.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Sala sala;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Miembro miembro;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime fin;

    @Column(name = "attendees", nullable = false)
    private int asistentes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EstadoReserva estado;

    @Column(name = "room_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoSala = BigDecimal.ZERO;

    @Column(name = "services_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoServicios = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoTotal = BigDecimal.ZERO;

    @Column(name = "cancellation_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal cargoCancelacion = BigDecimal.ZERO;

    @Column(name = "consumed_minutes", nullable = false)
    private int minutosConsumidos;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetalleReserva> detalles = new ArrayList<>();

    protected Reserva() {
        // Requerido por JPA.
    }

    public Reserva(Sala sala, Miembro miembro, LocalDateTime inicio, LocalDateTime fin, int asistentes,
                   EstadoReserva estado) {
        this.sala = sala;
        this.miembro = miembro;
        this.inicio = inicio;
        this.fin = fin;
        this.asistentes = asistentes;
        this.estado = estado;
        this.minutosConsumidos = (int) Duration.between(inicio, fin).toMinutes();
    }

    public void agregarDetalle(DetalleReserva detalle) {
        detalles.add(detalle);
        detalle.setReserva(this);
    }

    public void quitarDetalle(DetalleReserva detalle) {
        detalles.remove(detalle);
    }

    public long getDuracionMinutos() {
        return Duration.between(inicio, fin).toMinutes();
    }

    public Long getId() {
        return id;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }

    public Miembro getMiembro() {
        return miembro;
    }

    public void setMiembro(Miembro miembro) {
        this.miembro = miembro;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public void setFin(LocalDateTime fin) {
        this.fin = fin;
    }

    public int getAsistentes() {
        return asistentes;
    }

    public void setAsistentes(int asistentes) {
        this.asistentes = asistentes;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public BigDecimal getCostoSala() {
        return costoSala;
    }

    public void setCostoSala(BigDecimal costoSala) {
        this.costoSala = costoSala;
    }

    public BigDecimal getCostoServicios() {
        return costoServicios;
    }

    public void setCostoServicios(BigDecimal costoServicios) {
        this.costoServicios = costoServicios;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public BigDecimal getCargoCancelacion() {
        return cargoCancelacion;
    }

    public void setCargoCancelacion(BigDecimal cargoCancelacion) {
        this.cargoCancelacion = cargoCancelacion;
    }

    public int getMinutosConsumidos() {
        return minutosConsumidos;
    }

    public void setMinutosConsumidos(int minutosConsumidos) {
        this.minutosConsumidos = minutosConsumidos;
    }

    public List<DetalleReserva> getDetalles() {
        return detalles;
    }
}
