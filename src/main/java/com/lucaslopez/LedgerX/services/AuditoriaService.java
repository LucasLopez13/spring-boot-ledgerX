package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.logActivitys.ActivityLog;
import com.lucaslopez.LedgerX.domain.logActivitys.LogActivityRepository;
import com.lucaslopez.LedgerX.domain.logActivitys.TipoAccion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditoriaService {

    @Autowired
    private LogActivityRepository logActivityRepository;

    // Usamos @Async para que guardar el log NO frene la operación principal.
    // Si Mongo está lento, el usuario no lo nota.
    @Async
    public void registrarActividad(Long idUsuario, TipoAccion accion, String detalle, String ip) {
        var log = ActivityLog.builder()
                .idUsuario(idUsuario)
                .accion(accion)
                .direccionIp(ip)
                .metadata(Map.of("detalle", detalle))
                .fechaCreacion(LocalDateTime.now())
                .build();

        logActivityRepository.save(log);
    }
}
