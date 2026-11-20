package com.tdea.mcp_banco.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "cuentas")
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero;

    @Column(nullable = false)
    private String titular;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal saldo;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    public Cuenta() { }

    public Cuenta(String numero, String titular, BigDecimal saldo) {
        this.numero = numero;
        this.titular = titular;
        this.saldo = saldo;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public String getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
