package com.lucaslopez.LedgerX.transaccion.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /**
     Recupera el historial de transacciones en las que una billetera participó (ya
     sea como origen o como destino).
     Se utiliza JPQL para unificar ambos criterios (OR) y ordenar los resultados
     de forma descendente,
     favoreciendo la vista de movimientos recientes en el Dashboard.
     Esta consulta soporta Paginación automática inyectando un objeto Pageable
     desde el Controlador.
     */
    @Query("""
                    SELECT t from Transaccion t
                    WHERE t.billeteraOrigen.id = :idBilletera
                    OR t.billeteraDestino.id = :idBilletera
                    ORDER BY t.createdAt DESC
            """)
    Page<Transaccion> buscarHistorialPorBilletera(Long idBilletera, Pageable pageable);
}
