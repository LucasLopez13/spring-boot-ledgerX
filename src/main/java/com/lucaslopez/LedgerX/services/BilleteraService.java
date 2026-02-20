package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.domain.billeteras.DatosDetalleSaldo;
import com.lucaslopez.LedgerX.domain.logActivitys.TipoAccion;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BilleteraService {

    @Autowired
    private BilleteraRepository repository;
    @Autowired
    private AuditoriaService auditoriaService;

    public void crearBilleteraInicial(Usuario usuario) {
        var saldoInicial = BigDecimal.ZERO;
        var cbu = generarCbuUnico();

        var billetera = Billetera.builder()
                .usuario(usuario)
                .saldo(saldoInicial)
                .cbu(cbu)
                .build();

        repository.save(billetera);
    }

    // Genera un CBU de 22 dígitos único en la BD.
    // Extrae solo los dígitos del UUID (descarta letras a-f del hex) y rellena con
    // ceros si es necesario.
    // El loop garantiza unicidad ante la (muy improbable) colisión de CBUs.
    private String generarCbuUnico() {
        String cbu;
        do {
            // UUID genera 32 hex chars — tomamos solo dígitos y recortamos a 22
            cbu = java.util.UUID.randomUUID().toString()
                    .replace("-", "")
                    .chars()
                    .filter(Character::isDigit)
                    .limit(22)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
            // Rellenar con ceros si cayeron menos de 22 dígitos (raro pero posible)
            while (cbu.length() < 22)
                cbu += "0";
        } while (repository.findByCbu(cbu).isPresent());
        return cbu;
    }

    public DatosDetalleSaldo consultarSaldo(Long usuarioId) {
        var billetera = repository.findByUsuarioId(usuarioId);

        if (billetera == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }

        auditoriaService.registrarActividad(
                usuarioId,
                TipoAccion.CONSULTA_SALDO,
                "Consulta de Saldo",
                "127.0.0.1");

        return new DatosDetalleSaldo(billetera);
    }
}
