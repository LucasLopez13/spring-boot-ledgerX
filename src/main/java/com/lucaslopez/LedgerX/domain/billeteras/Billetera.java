package com.lucaslopez.LedgerX.domain.billeteras;

import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "billeteras")
@Entity(name = "Billetera")
// @EntityListeners necesario para que @CreatedDate y @LastModifiedDate se
// pueblen automáticamente.
@EntityListeners(AuditingEntityListener.class)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Billetera {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal saldo;
    // CBU inmutable: se genera una sola vez al crear la billetera.
    // No tiene @Setter para que no pueda modificarse después.
    private String cbu;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    // Optimistic Locking para problemas de concurrencia.
    @Version
    private Long version;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Billetera(Usuario usuario, BigDecimal saldo) {
        this.id = null;
        this.usuario = usuario;
        this.saldo = saldo;
    }

    public void debitar(BigDecimal monto) {
        this.saldo = this.saldo.subtract(monto);
    }

    public void depositar(BigDecimal monto) {
        this.saldo = this.saldo.add(monto);
    }
}
