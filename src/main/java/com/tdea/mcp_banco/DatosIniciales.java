package com.tdea.mcp_banco;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.tdea.mcp_banco.repository.CuentaRepository;
import com.tdea.mcp_banco.service.BancoService;

/** Si la base de datos está vacía, crea dos cuentas de demostración. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private final CuentaRepository cuentas;
    private final BancoService banco;

    public DatosIniciales(CuentaRepository cuentas, BancoService banco) {
        this.cuentas = cuentas;
        this.banco = banco;
    }

    @Override
    public void run(String... args) {
        if (cuentas.count() == 0) {
            banco.crearCuenta("Ana Gómez", 1_000_000);
            banco.crearCuenta("Carlos Pérez", 500_000);
        }
    }
}
