package com.empresa.polizas.entity;

import com.empresa.polizas.enums.EstadoPoliza;
import com.empresa.polizas.enums.TipoPoliza;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Poliza {

    private static final int ESCALA_MONETARIA = 2;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numeroPoliza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal canon;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal prima;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL)
    private List<Riesgo> riesgos = new ArrayList<>();

    protected Poliza() {
    }

    public Poliza(String numeroPoliza, TipoPoliza tipo, BigDecimal canon, BigDecimal prima) {
        this.numeroPoliza = numeroPoliza;
        this.tipo = tipo;
        this.canon = canon;
        this.prima = prima;
        this.estado = EstadoPoliza.ACTIVA;
    }

    public void agregarRiesgo(Riesgo riesgo) {
        riesgos.add(riesgo);
        riesgo.setPoliza(this);
    }

    public void cancelar() {
        this.estado = EstadoPoliza.CANCELADA;
        riesgos.forEach(Riesgo::cancelar);
    }

    public void renovar(BigDecimal ipc) {
        BigDecimal factor = BigDecimal.ONE.add(ipc);
        this.canon = canon.multiply(factor).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        this.prima = prima.multiply(factor).setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        this.estado = EstadoPoliza.RENOVADA;
    }

    public boolean estaCancelada() {
        return estado == EstadoPoliza.CANCELADA;
    }

    public boolean esIndividual() {
        return tipo == TipoPoliza.INDIVIDUAL;
    }

    public Long getId() { return id; }
    public String getNumeroPoliza() { return numeroPoliza; }
    public TipoPoliza getTipo() { return tipo; }
    public EstadoPoliza getEstado() { return estado; }
    public BigDecimal getCanon() { return canon; }
    public BigDecimal getPrima() { return prima; }

    public List<Riesgo> getRiesgos() {
        return Collections.unmodifiableList(riesgos);
    }
}