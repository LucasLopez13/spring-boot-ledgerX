package com.lucaslopez.LedgerX.billetera.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BilleteraRepository extends JpaRepository<Billetera, Long> {
    Billetera findByUsuarioId(Long usuarioId);

    Optional<Billetera> findByCbu(String cbu);

    /**
     PESSIMISTIC_WRITE: Aplica un bloqueo a nivel de la base de datos (SELECT ...
     FOR UPDATE).
     Garantiza que ningún otro hilo o nodo concurrente pueda leer o
     modificar esta fila
     de la billetera hasta que la transacción en curso haga un commit o un
     rollback.
     Esto es vital para evitar condiciones de carrera donde
     dos peticiones
     intenten retirar dinero o transferir simultáneamente superando el saldo real.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Billetera b WHERE b.id = :id")
    Optional<Billetera> findByIdWithLock(Long id);

    /**
     Aplica el mismo mecanismo de Bloqueo Pesimista pero buscando la
     billetera a través de su CBU. Usado mayormente
     al procesar Transferencias donde el destino no siempre es propietario de la
     cuenta conectada.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Billetera b WHERE b.cbu = :cbu")
    Optional<Billetera> findByCbuWithLock(String cbu);
}
