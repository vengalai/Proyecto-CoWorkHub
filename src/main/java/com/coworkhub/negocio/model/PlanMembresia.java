package com.coworkhub.negocio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_plans")
public class PlanMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "included_hours_per_month", nullable = false)
    private int horasIncluidasMes;

    @Column(name = "overage_discount", nullable = false)
    private int descuentoExcedente;

    @Column(name = "max_active_bookings", nullable = false)
    private int maxReservasActivas;

    protected PlanMembresia() {
        // Requerido por JPA.
    }

    public PlanMembresia(String nombre, int horasIncluidasMes, int descuentoExcedente, int maxReservasActivas) {
        this.nombre = nombre;
        this.horasIncluidasMes = horasIncluidasMes;
        this.descuentoExcedente = descuentoExcedente;
        this.maxReservasActivas = maxReservasActivas;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getHorasIncluidasMes() {
        return horasIncluidasMes;
    }

    public void setHorasIncluidasMes(int horasIncluidasMes) {
        this.horasIncluidasMes = horasIncluidasMes;
    }

    public int getDescuentoExcedente() {
        return descuentoExcedente;
    }

    public void setDescuentoExcedente(int descuentoExcedente) {
        this.descuentoExcedente = descuentoExcedente;
    }

    public int getMaxReservasActivas() {
        return maxReservasActivas;
    }

    public void setMaxReservasActivas(int maxReservasActivas) {
        this.maxReservasActivas = maxReservasActivas;
    }
}
