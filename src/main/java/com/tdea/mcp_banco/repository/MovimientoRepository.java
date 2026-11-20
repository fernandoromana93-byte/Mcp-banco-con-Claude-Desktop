package com.tdea.mcp_banco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tdea.mcp_banco.model.Movimiento;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    List<Movimiento> findByCuentaOrigenOrCuentaDestinoOrderByFechaDesc(String origen, String destino);
}
