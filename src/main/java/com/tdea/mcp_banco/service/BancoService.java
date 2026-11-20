package com.tdea.mcp_banco.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdea.mcp_banco.dto.Dtos.CreditoDto;
import com.tdea.mcp_banco.dto.Dtos.CuentaDto;
import com.tdea.mcp_banco.dto.Dtos.MovimientoDto;
import com.tdea.mcp_banco.model.Cuenta;
import com.tdea.mcp_banco.model.Movimiento;
import com.tdea.mcp_banco.model.SolicitudCredito;
import com.tdea.mcp_banco.repository.CuentaRepository;
import com.tdea.mcp_banco.repository.MovimientoRepository;
import com.tdea.mcp_banco.repository.SolicitudCreditoRepository;

/**
 * Lógica de negocio bancaria. Cada método es una transacción de base de datos:
 * si algo falla (por ejemplo, saldo insuficiente) se hace rollback completo.
 */
@Service
@Transactional
public class BancoService {

    private static final BigDecimal TASA_MENSUAL = new BigDecimal("0.0150"); // 1.5 % mensual
    private static final BigDecimal CREDITO_MIN = new BigDecimal("100000");
    private static final BigDecimal CREDITO_MAX = new BigDecimal("50000000");

    private final CuentaRepository cuentas;
    private final MovimientoRepository movimientos;
    private final SolicitudCreditoRepository creditos;

    public BancoService(CuentaRepository cuentas, MovimientoRepository movimientos,
                        SolicitudCreditoRepository creditos) {
        this.cuentas = cuentas;
        this.movimientos = movimientos;
        this.creditos = creditos;
    }

    // ---------- Cuentas ----------

    public CuentaDto crearCuenta(String titular, double depositoInicial) {
        if (titular == null || titular.isBlank()) {
            throw new IllegalArgumentException("El nombre del titular es obligatorio.");
        }
        if (depositoInicial < 0) {
            throw new IllegalArgumentException("El depósito inicial no puede ser negativo.");
        }
        BigDecimal saldo = BigDecimal.valueOf(depositoInicial).setScale(2, RoundingMode.HALF_UP);
        Cuenta cuenta = cuentas.save(new Cuenta(generarNumero(), titular.trim(), saldo));
        if (saldo.signum() > 0) {
            movimientos.save(new Movimiento("APERTURA", null, cuenta.getNumero(), saldo,
                    "Depósito inicial de apertura"));
        }
        return CuentaDto.de(cuenta);
    }

    @Transactional(readOnly = true)
    public CuentaDto consultarCuenta(String numero) {
        return CuentaDto.de(buscar(numero));
    }

    @Transactional(readOnly = true)
    public List<CuentaDto> listarCuentas() {
        return cuentas.findAll().stream().map(CuentaDto::de).toList();
    }

    // ---------- Transacciones ----------

    public MovimientoDto consignar(String numero, double monto, String descripcion) {
        BigDecimal valor = validarMonto(monto);
        Cuenta cuenta = buscarBloqueada(numero);
        cuenta.setSaldo(cuenta.getSaldo().add(valor));
        Movimiento m = movimientos.save(new Movimiento("CONSIGNACION", null, cuenta.getNumero(),
                valor, textoODefecto(descripcion, "Consignación en cuenta")));
        return MovimientoDto.de(m);
    }

    public MovimientoDto retirar(String numero, double monto, String descripcion) {
        BigDecimal valor = validarMonto(monto);
        Cuenta cuenta = buscarBloqueada(numero);
        validarFondos(cuenta, valor);
        cuenta.setSaldo(cuenta.getSaldo().subtract(valor));
        Movimiento m = movimientos.save(new Movimiento("RETIRO", cuenta.getNumero(), null,
                valor, textoODefecto(descripcion, "Retiro de efectivo")));
        return MovimientoDto.de(m);
    }

    public MovimientoDto transferir(String origen, String destino, double monto, String descripcion) {
        BigDecimal valor = validarMonto(monto);
        if (origen == null || destino == null || origen.equals(destino)) {
            throw new IllegalArgumentException("La cuenta origen y destino deben ser distintas.");
        }
        // Bloqueamos siempre en el mismo orden para evitar deadlocks entre transferencias cruzadas.
        Cuenta primera;
        Cuenta segunda;
        if (origen.compareTo(destino) < 0) {
            primera = buscarBloqueada(origen);
            segunda = buscarBloqueada(destino);
        } else {
            segunda = buscarBloqueada(destino);
            primera = buscarBloqueada(origen);
        }
        Cuenta cOrigen = primera.getNumero().equals(origen) ? primera : segunda;
        Cuenta cDestino = primera.getNumero().equals(origen) ? segunda : primera;

        validarFondos(cOrigen, valor);
        cOrigen.setSaldo(cOrigen.getSaldo().subtract(valor));
        cDestino.setSaldo(cDestino.getSaldo().add(valor));

        Movimiento m = movimientos.save(new Movimiento("TRANSFERENCIA", origen, destino, valor,
                textoODefecto(descripcion, "Transferencia entre cuentas")));
        return MovimientoDto.de(m);
    }

    @Transactional(readOnly = true)
    public List<MovimientoDto> historial(String numero) {
        buscar(numero); // valida que exista
        return movimientos.findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(numero, numero)
                .stream().limit(20).map(MovimientoDto::de).toList();
    }

    // ---------- Créditos ----------

    /**
     * Regla de negocio simple (académica): se aprueba si el monto está entre $100.000 y $50.000.000,
     * el plazo entre 6 y 72 meses y el saldo de la cuenta es al menos el 10 % del monto solicitado.
     * Si se aprueba, el dinero se desembolsa en la cuenta.
     */
    public CreditoDto solicitarCredito(String numero, double monto, int plazoMeses) {
        BigDecimal valor = validarMonto(monto);
        if (plazoMeses < 6 || plazoMeses > 72) {
            throw new IllegalArgumentException("El plazo debe estar entre 6 y 72 meses.");
        }
        Cuenta cuenta = buscarBloqueada(numero);
        BigDecimal cuota = calcularCuota(valor, plazoMeses);

        String estado = "APROBADA";
        String motivo = "Crédito aprobado y desembolsado en la cuenta.";
        if (valor.compareTo(CREDITO_MIN) < 0 || valor.compareTo(CREDITO_MAX) > 0) {
            estado = "RECHAZADA";
            motivo = "El monto debe estar entre " + CREDITO_MIN + " y " + CREDITO_MAX + ".";
        } else if (cuenta.getSaldo().compareTo(valor.multiply(new BigDecimal("0.10"))) < 0) {
            estado = "RECHAZADA";
            motivo = "El saldo de la cuenta debe ser al menos el 10 % del monto solicitado.";
        }

        SolicitudCredito solicitud = creditos.save(new SolicitudCredito(
                cuenta.getNumero(), valor, plazoMeses, TASA_MENSUAL, cuota, estado, motivo));

        if ("APROBADA".equals(estado)) {
            cuenta.setSaldo(cuenta.getSaldo().add(valor));
            movimientos.save(new Movimiento("DESEMBOLSO_CREDITO", null, cuenta.getNumero(), valor,
                    "Desembolso del crédito #" + solicitud.getId()));
        }
        return CreditoDto.de(solicitud);
    }

    @Transactional(readOnly = true)
    public List<CreditoDto> listarCreditos(String numero) {
        buscar(numero);
        return creditos.findByNumeroCuentaOrderByFechaDesc(numero).stream().map(CreditoDto::de).toList();
    }

    // ---------- Utilidades ----------

    /** Cuota fija (sistema francés): P * r / (1 - (1 + r)^-n) */
    private BigDecimal calcularCuota(BigDecimal principal, int meses) {
        double r = TASA_MENSUAL.doubleValue();
        double cuota = principal.doubleValue() * r / (1 - Math.pow(1 + r, -meses));
        return BigDecimal.valueOf(cuota).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal validarMonto(double monto) {
        if (Double.isNaN(monto) || Double.isInfinite(monto) || monto <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        return BigDecimal.valueOf(monto).setScale(2, RoundingMode.HALF_UP);
    }

    private void validarFondos(Cuenta cuenta, BigDecimal valor) {
        if (cuenta.getSaldo().compareTo(valor) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente. Saldo actual: " + cuenta.getSaldo());
        }
    }

    private Cuenta buscar(String numero) {
        return cuentas.findByNumero(numero == null ? "" : numero.trim())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta '" + numero + "' no existe."));
    }

    private Cuenta buscarBloqueada(String numero) {
        return cuentas.buscarParaActualizar(numero == null ? "" : numero.trim())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta '" + numero + "' no existe."));
    }

    private String generarNumero() {
        String numero;
        do {
            numero = String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
        } while (cuentas.existsByNumero(numero));
        return numero;
    }

    private String textoODefecto(String texto, String defecto) {
        return (texto == null || texto.isBlank()) ? defecto : texto.trim();
    }
}
