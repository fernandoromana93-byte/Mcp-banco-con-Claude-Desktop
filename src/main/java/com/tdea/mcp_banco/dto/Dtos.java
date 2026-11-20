package com.tdea.mcp_banco.dto;

import java.math.BigDecimal;

import com.tdea.mcp_banco.model.Cuenta;
import com.tdea.mcp_banco.model.Movimiento;
import com.tdea.mcp_banco.model.SolicitudCredito;

/** Objetos simples que se serializan a JSON y se devuelven al modelo de IA. */
public final class Dtos {

    private Dtos() { }

    public record CuentaDto(String numero, String titular, BigDecimal saldo) {
        public static CuentaDto de(Cuenta c) {
            return new CuentaDto(c.getNumero(), c.getTitular(), c.getSaldo());
        }
    }

    public record MovimientoDto(Long id, String tipo, String cuentaOrigen, String cuentaDestino,
                                BigDecimal monto, String descripcion, String fecha) {
        public static MovimientoDto de(Movimiento m) {
            return new MovimientoDto(m.getId(), m.getTipo(), m.getCuentaOrigen(), m.getCuentaDestino(),
                    m.getMonto(), m.getDescripcion(), m.getFecha().toString());
        }
    }

    public record CreditoDto(Long id, String numeroCuenta, BigDecimal monto, int plazoMeses,
                             BigDecimal tasaMensual, BigDecimal cuotaMensual,
                             String estado, String motivo, String fecha) {
        public static CreditoDto de(SolicitudCredito s) {
            return new CreditoDto(s.getId(), s.getNumeroCuenta(), s.getMonto(), s.getPlazoMeses(),
                    s.getTasaMensual(), s.getCuotaMensual(), s.getEstado(), s.getMotivo(),
                    s.getFecha().toString());
        }
    }
}
