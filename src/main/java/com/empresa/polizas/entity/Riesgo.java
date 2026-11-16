package com.empresa.polizas.entity;

import com.empresa.polizas.enums.EstadoRiesgo;
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

@Entity
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id")
    private Poliza poliza;

    protected Riesgo() {
    }

    public Riesgo(String descripcion) {
        this.descripcion = descripcion;
        this.estado = EstadoRiesgo.ACTIVA;
    }

    public void cancelar() {
        this.estado = EstadoRiesgo.CANCELADA;
    }

    public boolean estaCancelado() {
        return estado == EstadoRiesgo.CANCELADA;
    }

    void setPoliza(Poliza poliza) {
        this.poliza = poliza;
    }

    public Long getId() { return id; }
    public String getDescripcion() { return descripcion; }
    public EstadoRiesgo getEstado() { return estado; }
    public Poliza getPoliza() { return poliza; }
}