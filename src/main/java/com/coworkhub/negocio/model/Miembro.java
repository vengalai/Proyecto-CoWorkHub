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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Miembro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanMembresia plan;

    @Column(name = "document_number", nullable = false, unique = true, length = 30)
    private String numeroDocumento;

    @Column(name = "full_name", nullable = false, length = 120)
    private String nombreCompleto;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EstadoMiembro estado;

    @OneToOne(mappedBy = "miembro", fetch = FetchType.LAZY)
    private CuentaUsuario cuenta;

    protected Miembro() {
        // Requerido por JPA.
    }

    public Miembro(PlanMembresia plan, String numeroDocumento, String nombreCompleto, String email,
                   EstadoMiembro estado) {
        this.plan = plan;
        this.numeroDocumento = numeroDocumento;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public PlanMembresia getPlan() {
        return plan;
    }

    public void setPlan(PlanMembresia plan) {
        this.plan = plan;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EstadoMiembro getEstado() {
        return estado;
    }

    public void setEstado(EstadoMiembro estado) {
        this.estado = estado;
    }

    public CuentaUsuario getCuenta() {
        return cuenta;
    }
}
