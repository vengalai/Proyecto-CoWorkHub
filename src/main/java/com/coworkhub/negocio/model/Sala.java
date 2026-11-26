package com.coworkhub.negocio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rooms", uniqueConstraints = {
        @UniqueConstraint(name = "uk_rooms_location_name", columnNames = {"location_id", "name"})
})
public class Sala {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Sede sede;

    @Column(name = "name", nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private TipoSala tipo;

    @Column(name = "capacity", nullable = false)
    private int capacidad;

    @Column(name = "hourly_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaPorHora;

    @Column(name = "active", nullable = false)
    private boolean activa;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_equipment",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipamiento> equipamientos = new HashSet<>();

    protected Sala() {
        // Requerido por JPA.
    }

    public Sala(Sede sede, String nombre, TipoSala tipo, int capacidad, BigDecimal tarifaPorHora, boolean activa) {
        this.sede = sede;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.tarifaPorHora = tarifaPorHora;
        this.activa = activa;
    }

    public void agregarEquipamiento(Equipamiento equipamiento) {
        this.equipamientos.add(equipamiento);
    }

    public void quitarEquipamiento(Equipamiento equipamiento) {
        this.equipamientos.remove(equipamiento);
    }

    public Long getId() {
        return id;
    }

    public Sede getSede() {
        return sede;
    }

    public void setSede(Sede sede) {
        this.sede = sede;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoSala getTipo() {
        return tipo;
    }

    public void setTipo(TipoSala tipo) {
        this.tipo = tipo;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public BigDecimal getTarifaPorHora() {
        return tarifaPorHora;
    }

    public void setTarifaPorHora(BigDecimal tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Set<Equipamiento> getEquipamientos() {
        return equipamientos;
    }
}
