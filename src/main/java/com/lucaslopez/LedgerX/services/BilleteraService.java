package com.lucaslopez.LedgerX.services;

import com.lucaslopez.LedgerX.domain.billeteras.Billetera;
import com.lucaslopez.LedgerX.domain.billeteras.BilleteraRepository;
import com.lucaslopez.LedgerX.domain.billeteras.DatosDetalleSaldo;
import com.lucaslopez.LedgerX.domain.logActivitys.TipoAccion;
import com.lucaslopez.LedgerX.domain.usuarios.Usuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

        var billetera = new Billetera(usuario, saldoInicial);
        repository.save(billetera);
    }

    public DatosDetalleSaldo consultarSaldo(Long usuarioId) {
        var billetera =  repository.findByUsuarioId(usuarioId);

        if (billetera == null) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }

        auditoriaService.registrarActividad(
                usuarioId,
                TipoAccion.CONSULTA_SALDO,
                "Consulta de Saldo",
                "127.0.0.1"
        );

        return new DatosDetalleSaldo(billetera);
    }
}
