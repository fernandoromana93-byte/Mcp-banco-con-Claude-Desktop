package com.tdea.mcp_banco.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tdea.mcp_banco.model.SolicitudCredito;

public interface SolicitudCreditoRepository extends JpaRepository<SolicitudCredito, Long> {

    List<SolicitudCredito> findByNumeroCuentaOrderByFechaDesc(String numeroCuenta);
}
