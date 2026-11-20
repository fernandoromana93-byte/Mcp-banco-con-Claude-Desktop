package com.tdea.mcp_banco.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.tdea.mcp_banco.dto.Respuesta;
import com.tdea.mcp_banco.service.BancoService;

@Component
public class BancoTools {

    private final BancoService banco;

    public BancoTools(BancoService banco) {
        this.banco = banco;
    }

    @Tool(name = "crearCuenta",
          description = "Crea una cuenta bancaria nueva. Requiere el nombre del titular y un depósito inicial (puede ser 0). Devuelve el número de cuenta generado.")
    public Respuesta crearCuenta(
            @ToolParam(description = "Nombre completo del titular de la cuenta") String titular,
            @ToolParam(description = "Depósito inicial en pesos, 0 o mayor") double depositoInicial) {
        try {
            return Respuesta.ok(banco.crearCuenta(titular, depositoInicial));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "listarCuentas",
          description = "Lista todas las cuentas bancarias existentes con número, titular y saldo.")
    public Respuesta listarCuentas() {
        return Respuesta.ok(banco.listarCuentas());
    }

    @Tool(name = "consultarSaldo",
          description = "Consulta el titular y el saldo actual de una cuenta a partir de su número de cuenta.")
    public Respuesta consultarSaldo(
            @ToolParam(description = "Número de cuenta de 10 dígitos") String numeroCuenta) {
        try {
            return Respuesta.ok(banco.consultarCuenta(numeroCuenta));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "consignar",
          description = "Realiza una consignación (depósito) de dinero en una cuenta. Aumenta el saldo de la cuenta.")
    public Respuesta consignar(
            @ToolParam(description = "Número de la cuenta que recibe el dinero") String numeroCuenta,
            @ToolParam(description = "Monto a consignar en pesos, mayor que cero") double monto,
            @ToolParam(description = "Descripción opcional de la consignación", required = false) String descripcion) {
        try {
            return Respuesta.ok(banco.consignar(numeroCuenta, monto, descripcion));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "retirar",
          description = "Realiza un retiro de dinero de una cuenta. Falla si el saldo es insuficiente.")
    public Respuesta retirar(
            @ToolParam(description = "Número de la cuenta de la que se retira") String numeroCuenta,
            @ToolParam(description = "Monto a retirar en pesos, mayor que cero") double monto,
            @ToolParam(description = "Descripción opcional del retiro", required = false) String descripcion) {
        try {
            return Respuesta.ok(banco.retirar(numeroCuenta, monto, descripcion));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "transferir",
          description = "Transfiere dinero entre dos cuentas. Descuenta de la cuenta origen y acredita en la destino de forma atómica. Falla si el saldo es insuficiente o alguna cuenta no existe.")
    public Respuesta transferir(
            @ToolParam(description = "Número de la cuenta origen (de donde sale el dinero)") String cuentaOrigen,
            @ToolParam(description = "Número de la cuenta destino (a donde llega el dinero)") String cuentaDestino,
            @ToolParam(description = "Monto a transferir en pesos, mayor que cero") double monto,
            @ToolParam(description = "Descripción opcional de la transferencia", required = false) String descripcion) {
        try {
            return Respuesta.ok(banco.transferir(cuentaOrigen, cuentaDestino, monto, descripcion));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "consultarMovimientos",
          description = "Devuelve los últimos 20 movimientos (consignaciones, retiros, transferencias y desembolsos) de una cuenta.")
    public Respuesta consultarMovimientos(
            @ToolParam(description = "Número de cuenta") String numeroCuenta) {
        try {
            return Respuesta.ok(banco.historial(numeroCuenta));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "solicitarCredito",
          description = "Crea una solicitud de crédito para una cuenta. Se aprueba automáticamente si el monto está entre 100.000 y 50.000.000, el plazo entre 6 y 72 meses y el saldo es al menos el 10% del monto; si se aprueba, el dinero se desembolsa en la cuenta. Devuelve estado, cuota mensual y motivo.")
    public Respuesta solicitarCredito(
            @ToolParam(description = "Número de la cuenta del solicitante") String numeroCuenta,
            @ToolParam(description = "Monto del crédito en pesos") double monto,
            @ToolParam(description = "Plazo en meses, entre 6 y 72") int plazoMeses) {
        try {
            return Respuesta.ok(banco.solicitarCredito(numeroCuenta, monto, plazoMeses));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }

    @Tool(name = "consultarCreditos",
          description = "Lista las solicitudes de crédito (aprobadas y rechazadas) de una cuenta.")
    public Respuesta consultarCreditos(
            @ToolParam(description = "Número de cuenta") String numeroCuenta) {
        try {
            return Respuesta.ok(banco.listarCreditos(numeroCuenta));
        } catch (IllegalArgumentException e) {
            return Respuesta.error(e.getMessage());
        }
    }
}