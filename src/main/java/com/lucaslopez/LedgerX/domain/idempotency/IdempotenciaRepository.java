package com.lucaslopez.LedgerX.domain.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotenciaRepository extends JpaRepository<Idempotencia, Long> {

    Optional<Idempotencia> findByIdempotenciaKey(String idempotenciaKey);
}
