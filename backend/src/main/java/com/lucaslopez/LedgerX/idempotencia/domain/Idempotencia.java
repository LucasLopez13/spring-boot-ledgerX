package com.lucaslopez.LedgerX.idempotencia.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "idempotencia_keys")
@Entity(name = "Idempotencia")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Builder
public class Idempotencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String idempotenciaKey;
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    private IdempotenciaStatus status;

    private Integer responseStatus;
    private String responseBody;

    @CreatedDate
    private LocalDateTime createdAt;
}
