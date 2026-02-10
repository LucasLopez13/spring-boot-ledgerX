package com.lucaslopez.LedgerX.domain.logActivitys;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@Document(collection = "activity_logs")
public class ActivityLog {

    @Id
    private String id;
    private Long idUsuario;
    private TipoAccion accion;
    private String direccionIp;

    private Map<String, Object> metadata;

    private LocalDateTime fechaCreacion;
}
