package com.lucaslopez.LedgerX.domain.transacciones;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    @Query("""
        SELECT t from Transaccion t
        WHERE t.billeteraOrigen.id = :idBilletera
        OR t.billeteraDestino.id = :idBilletera
        ORDER BY t.createdAt DESC
""")
    Page<Transaccion> buscarHistorialPorBilletera(Long idBilletera, Pageable pageable);
}
