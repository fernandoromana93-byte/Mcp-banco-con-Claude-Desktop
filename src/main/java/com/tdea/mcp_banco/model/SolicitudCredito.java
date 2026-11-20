package com.tdea.mcp_banco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "solicitudes_credito")
public class SolicitudCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numeroCuenta;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private int plazoMeses;

    /** Tasa mensual aplicada, por ejemplo 0.015 = 1.5 % mensual. */
    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal tasaMensual;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal cuotaMensual;

    /** APROBADA o RECHAZADA */
    @Column(nullable = false)
    private String estado;

    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public SolicitudCredito() { }

    public SolicitudCredito(String numeroCuenta, BigDecimal monto, int plazoMeses,
                            BigDecimal tasaMensual, BigDecimal cuotaMensual,
                            String estado, String motivo) {
        this.numeroCuenta = numeroCuenta;
        this.monto = monto;
        this.plazoMeses = plazoMeses;
        this.tasaMensual = tasaMensual;
        this.cuotaMensual = cuotaMensual;
        this.estado = estado;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNumeroCuenta() { return numeroCuenta; }
    public BigDecimal getMonto() { return monto; }
    public int getPlazoMeses() { return plazoMeses; }
    public BigDecimal getTasaMensual() { return tasaMensual; }
    public BigDecimal getCuotaMensual() { return cuotaMensual; }
    public String getEstado() { return estado; }
    public String getMotivo() { return motivo; }
    public LocalDateTime getFecha() { return fecha; }
}
