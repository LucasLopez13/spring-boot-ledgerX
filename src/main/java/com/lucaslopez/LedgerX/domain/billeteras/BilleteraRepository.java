package com.lucaslopez.LedgerX.domain.billeteras;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BilleteraRepository extends JpaRepository<Billetera, Long> {
    Billetera findByUsuarioId(Long usuarioId);

    // PESSIMISTIC_WRITE: Bloquea la fila. Nadie puede leer ni escribir hasta que termine la transacción.
    // Se evitan asi condiciones de carrera.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Billetera b WHERE b.id = :id")
    Optional<Billetera> findByIdWithLock(Long id);
}
