package com.tdea.mcp_banco.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tdea.mcp_banco.model.Cuenta;

import jakarta.persistence.LockModeType;

public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    Optional<Cuenta> findByNumero(String numero);

    boolean existsByNumero(String numero);

    /** Bloqueo pesimista: evita que dos operaciones modifiquen el mismo saldo a la vez. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cuenta c where c.numero = :numero")
    Optional<Cuenta> buscarParaActualizar(@Param("numero") String numero);
}
