package com.lucaslopez.LedgerX.transaccion.domain;

import com.lucaslopez.LedgerX.billetera.domain.Billetera;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "transacciones")
@Entity(name = "Transaccion")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billetera_origen_id")
    private Billetera billeteraOrigen;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billetera_destino_id")
    private Billetera billeteraDestino;
    private BigDecimal cantidad;
    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipoTransaccion;
    @Enumerated(EnumType.STRING)
    private EstadoTransaccion estadoTransaccion;
    @CreatedDate
    private LocalDateTime createdAt;
    private String detalleTransaccion;
}
