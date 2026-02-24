import React, { useState } from 'react';
import { transactionService } from '../services/wallet';
import './TransactionModal.css';

const TransactionModal = ({ tipo, estaAbierto, alCerrar, alTenerExito }) => {
    const [monto, setMonto] = useState('');
    const [cbuDestino, setCbuDestino] = useState('');
    const [descripcion, setDescripcion] = useState('');
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState('');

    if (!estaAbierto) return null;

    const manejarEnvio = async (e) => {
        e.preventDefault();
        setError('');
        setCargando(true);

        const cargaUtil = {
            tipoTransaccion: tipo,
            monto: parseFloat(monto),
            descripcion: descripcion || ''
        };

        if (tipo === 'TRANSFERENCIA') {
            cargaUtil.cbuDestino = cbuDestino;
        }

        try {
            await transactionService.realizarTransaccion(cargaUtil);
            alTenerExito();
            alCerrar();
            // Limpiar campos
            setMonto('');
            setCbuDestino('');
            setDescripcion('');
        } catch (err) {
            // Manejamos tanto hilos de texto como arrays de validación
            let msj = 'Error al procesar la transacción';
            if (typeof err.response?.data === 'string') {
                msj = err.response.data;
            } else if (Array.isArray(err.response?.data)) {
                msj = err.response.data[0].error;
            }
            setError(msj);
        } finally {
            setCargando(false);
        }
    };

    const obtenerTitulo = () => {
        if (tipo === 'DEPOSITO') return 'Realizar Depósito';
        if (tipo === 'RETIRO') return 'Solicitar Retiro';
        return 'Nueva Transferencia';
    };

    return (
        <div className="superposicion-modal">
            <div className="contenido-modal glass">
                <div className="cabecera-modal">
                    <h2>{obtenerTitulo()}</h2>
                    <button className="boton-cerrar" onClick={alCerrar}>&times;</button>
                </div>

                <form onSubmit={manejarEnvio}>
                    <div className="cuerpo-modal">
                        <div className="campo-entrada">
                            <label>Monto</label>
                            <div className="contenedor-entrada-monto">
                                <span className="moneda">$</span>
                                <input
                                    type="number"
                                    step="0.01"
                                    min="0.01"
                                    placeholder="0.00"
                                    value={monto}
                                    onChange={(e) => setMonto(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        {tipo === 'TRANSFERENCIA' && (
                            <div className="campo-entrada">
                                <label>CBU Destino</label>
                                <input
                                    type="text"
                                    placeholder="22 dígitos del destinatario"
                                    maxLength="22"
                                    value={cbuDestino}
                                    onChange={(e) => setCbuDestino(e.target.value)}
                                    required
                                />
                            </div>
                        )}

                        <div className="campo-entrada">
                            <label>Descripción (Opcional)</label>
                            <input
                                type="text"
                                placeholder="Motivo del movimiento"
                                value={descripcion}
                                onChange={(e) => setDescripcion(e.target.value)}
                            />
                        </div>

                        {error && <div className="error-modal">{error}</div>}
                    </div>

                    <div className="pie-modal">
                        <button type="button" className="boton-cancelar" onClick={alCerrar}>Cancelar</button>
                        <button type="submit" className="boton-confirmar" disabled={cargando}>
                            {cargando ? 'Procesando...' : 'Confirmar'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default TransactionModal;
