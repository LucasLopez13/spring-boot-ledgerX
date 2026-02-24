import React, { useState, useEffect } from 'react';
import { walletService, transactionService } from '../services/wallet';
import TransactionModal from '../components/TransactionModal';
import Profile from './Profile';
import './Dashboard.css';

const Dashboard = ({ alCerrarSesion }) => {
    const [datos, setDatos] = useState({ saldo: 0, cbu: '', nombreUsuario: '' });
    const [transacciones, setTransacciones] = useState([]);
    const [pagina, setPagina] = useState(0);
    const [totalPaginas, setTotalPaginas] = useState(1);
    const [cargando, setCargando] = useState(true);
    const [mostrarModal, setMostrarModal] = useState(false);
    const [vista, setVista] = useState('main'); // 'main', 'profile'
    const [tipoModal, setTipoModal] = useState('DEPOSITO');
    const [mostrarSaldo, setMostrarSaldo] = useState(true);
    const [reclamandoBono, setReclamandoBono] = useState(false);

    useEffect(() => {
        obtenerDatosIniciales(pagina);
    }, [pagina]);

    const obtenerDatosIniciales = async (currentPage = 0) => {
        try {
            const walletData = await walletService.getSaldo();
            const historial = await walletService.getHistorial(currentPage);
            setDatos(walletData);
            setTransacciones(historial.content || []);
            setTotalPaginas(historial.totalPages || 1);
        } catch (err) {
            console.error('Error fetching data', err);
        } finally {
            setCargando(false);
        }
    };

    const manejarReclamoBono = async () => {
        setReclamandoBono(true);
        try {
            await walletService.reclamarBono();
            await obtenerDatosIniciales(0);
        } catch (err) {
            console.error('Error al reclamar bono', err);
        } finally {
            setReclamandoBono(false);
        }
    };

    const mostrarBono = datos.saldo === 0 && transacciones.length === 0 && pagina === 0;

    const BotonAccion = ({ tipo, label, icon }) => (
        <button
            className={`action-btn ${tipo.toLowerCase()}`}
            onClick={() => {
                setTipoModal(tipo);
                setMostrarModal(true);
            }}
        >
            <span className="icon">{icon}</span>
            {label}
        </button>
    );

    const esDireccionPositiva = (t) => {
        if (t.tipoTransaccion === 'DEPOSITO') return true;
        if (t.tipoTransaccion === 'TRANSFERENCIA' && t.idCuentaDestino === datos.idBilletera) return true;
        return false;
    };

    if (vista === 'profile') {
        return <Profile alVolver={() => setVista('main')} />;
    }

    if (cargando) {
        return (
            <div className="dashboard-container">
                <header className="dashboard-header glass skeleton-box" style={{ height: '80px', marginBottom: '32px' }}></header>
                <main className="dashboard-main">
                    <div className="top-row">
                        <div className="balance-card glass skeleton-box" style={{ height: '200px' }}></div>
                        <div className="actions-card glass skeleton-box" style={{ height: '200px' }}></div>
                    </div>
                    <div className="history-card glass skeleton-box" style={{ height: '400px', marginTop: '32px' }}></div>
                </main>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            <header className="dashboard-header glass">
                <div className="user-info">
                    <h1>Bienvenido, <span>{datos.nombreUsuario || 'Usuario'}</span></h1>
                </div>
                <div className="header-actions">
                    <button className="profile-link-btn" onClick={() => setVista('profile')}>Mi Perfil</button>
                    <button className="logout-btn" onClick={alCerrarSesion}>Cerrar Sesión</button>
                </div>
            </header>

            <main className="dashboard-main">
                <div className="top-row">
                    <div className="balance-card glass">
                        <label>Saldo Disponible</label>
                        <div className="balance-wrapper">
                            <h2 className="balance-amount">
                                $ {mostrarSaldo ? datos.saldo.toLocaleString('es-AR') : '***'}<span> ARS</span>
                            </h2>
                            <button className="eye-btn" onClick={() => setMostrarSaldo(!mostrarSaldo)} title={mostrarSaldo ? "Ocultar saldo" : "Mostrar saldo"}>
                                {mostrarSaldo ? (
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                                ) : (
                                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                                )}
                            </button>
                        </div>
                        <div className="cbu-box">
                            <label>CBU:</label>
                            <code>{datos.cbu}</code>
                            <button className="copy-btn" onClick={() => navigator.clipboard.writeText(datos.cbu)}>
                                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                                    <rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect>
                                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <div className="actions-card glass">
                        <h3>Operaciones Rápidas</h3>
                        <div className="actions-grid">
                            <BotonAccion tipo="DEPOSITO" label="Depositar" icon="+" />
                            <BotonAccion tipo="RETIRO" label="Retirar" icon="-" />
                            <BotonAccion tipo="TRANSFERENCIA" label="Transferir" icon="⇄" />
                            {mostrarBono && (
                                <button
                                    className="action-btn bono-btn"
                                    onClick={manejarReclamoBono}
                                    disabled={reclamandoBono}
                                >
                                    <span className="icon">🎁</span>
                                    {reclamandoBono ? 'Reclamando...' : 'Reclamar Bono de Bienvenida'}
                                </button>
                            )}
                        </div>
                    </div>
                </div>

                <div className="history-card glass">
                    <div className="history-header">
                        <h3>Últimos Movimientos</h3>
                        <button className="refresh-btn" onClick={() => obtenerDatosIniciales(pagina)}>Actualizar</button>
                    </div>
                    <div className="table-responsive">
                        <table className="transaction-table">
                            <thead>
                                <tr>
                                    <th>Tipo</th>
                                    <th>Ref/Descripción</th>
                                    <th>Monto</th>
                                    <th>Fecha</th>
                                </tr>
                            </thead>
                            <tbody>
                                {transacciones.length > 0 ? transacciones.map(t => (
                                    <tr key={t.idTransaccion}>
                                        <td className="type-col">
                                            <span className={`badge ${t.tipoTransaccion.toLowerCase()}`}>
                                                {t.tipoTransaccion}
                                            </span>
                                        </td>
                                        <td className="desc-col">{t.descripcion || t.detalleTransaccion || '-'}</td>
                                        <td className={`amount-col ${esDireccionPositiva(t) ? 'pos' : 'neg'}`}>
                                            {esDireccionPositiva(t) ? '+' : '-'} $ {mostrarSaldo ? t.monto.toLocaleString('es-AR') : '***'}
                                        </td>
                                        <td className="date-col">
                                            {t.fechaDeCreacion
                                                ? t.fechaDeCreacion.substring(0, 16).replace(/-/g, '/')
                                                : '-'}
                                        </td>
                                    </tr>
                                )) : (
                                    <tr>
                                        <td colSpan="4" className="empty-state">
                                            <div className="empty-illustration">
                                                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--accent-teal)" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" style={{ opacity: 0.8 }}>
                                                    <rect x="2" y="5" width="20" height="14" rx="2"></rect>
                                                    <line x1="2" y1="10" x2="22" y2="10"></line>
                                                </svg>
                                                <p>Tu historial está en blanco.</p>
                                                <span>¡Añade fondos para empezar a operar!</span>
                                            </div>
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                    <div className="pagination-controls">
                        <button
                            className="page-btn"
                            disabled={pagina === 0}
                            onClick={() => setPagina(p => Math.max(0, p - 1))}
                        >
                            &larr; Atrás
                        </button>
                        <span className="page-indicator">Página {pagina + 1} de {totalPaginas}</span>
                        <button
                            className="page-btn"
                            disabled={pagina >= Math.max(totalPaginas - 1, 0)}
                            onClick={() => setPagina(p => Math.min(Math.max(totalPaginas - 1, 0), p + 1))}
                        >
                            Siguiente &rarr;
                        </button>
                    </div>
                </div>
            </main>

            <TransactionModal
                tipo={tipoModal}
                estaAbierto={mostrarModal}
                alCerrar={() => setMostrarModal(false)}
                alTenerExito={() => { setPagina(0); obtenerDatosIniciales(0); }}
            />
        </div>
    );
};

export default Dashboard;
