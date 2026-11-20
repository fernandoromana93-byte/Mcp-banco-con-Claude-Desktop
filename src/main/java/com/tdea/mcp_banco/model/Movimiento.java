package com.tdea.mcp_banco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "movimientos")
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** APERTURA, CONSIGNACION, RETIRO, TRANSFERENCIA, DESEMBOLSO_CREDITO */
    @Column(nullable = false)
    private String tipo;

    /** Cuenta que origina el movimiento (null en consignaciones y desembolsos). */
    private String cuentaOrigen;

    /** Cuenta que recibe el dinero (null en retiros). */
    private String cuentaDestino;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    private String descripcion;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Movimiento() { }

    public Movimiento(String tipo, String cuentaOrigen, String cuentaDestino,
                      BigDecimal monto, String descripcion) {
        this.tipo = tipo;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getTipo() { return tipo; }
    public String getCuentaOrigen() { return cuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }
    public BigDecimal getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFecha() { return fecha; }
}
