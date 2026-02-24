import React, { useState, useEffect } from 'react';
import api from '../services/api';
import './Profile.css';

const Profile = ({ alVolver }) => {
    const [datosFormulario, setDatosFormulario] = useState({
        nombre: '',
        apellido: '',
        email: '',
        passwordActual: '',
        contrasenia: '' // Nueva contraseña opcional
    });
    const [cargando, setCargando] = useState(false);
    const [error, setError] = useState('');
    const [exito, setExito] = useState('');

    useEffect(() => {
        obtenerPerfil();
    }, []);

    const obtenerPerfil = async () => {
        try {
            const response = await api.get('/usuarios/me');
            setDatosFormulario({
                ...datosFormulario,
                nombre: response.data.nombre,
                apellido: response.data.apellido,
                email: response.data.email
            });
        } catch (err) {
            console.error(err);
        }
    };

    const manejarCambio = (e) => {
        setDatosFormulario({ ...datosFormulario, [e.target.name]: e.target.value });
    };

    const manejarEnvio = async (e) => {
        e.preventDefault();
        setError('');
        setExito('');
        setCargando(true);

        try {
            const payload = { ...datosFormulario };
            if (!payload.contrasenia) delete payload.contrasenia;

            await api.put('/usuarios/me', payload);
            setExito('Perfil actualizado con éxito');
        } catch (err) {
            let msg = 'Error al actualizar perfil';
            if (typeof err.response?.data === 'string') {
                msg = err.response.data;
            } else if (Array.isArray(err.response?.data)) {
                msg = err.response.data[0].error;
            }
            setError(msg);
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="profile-container">
            <div className="profile-card glass">
                <div className="profile-header">
                    <h2>Información de Perfil</h2>
                    <button className="back-btn" onClick={alVolver}>&larr; Volver</button>
                </div>

                <form onSubmit={manejarEnvio}>
                    <div className="profile-grid">
                        <div className="input-field">
                            <label>Nombre</label>
                            <input name="nombre" value={datosFormulario.nombre} onChange={manejarCambio} required />
                        </div>
                        <div className="input-field">
                            <label>Apellido</label>
                            <input name="apellido" value={datosFormulario.apellido} onChange={manejarCambio} required />
                        </div>
                        <div className="input-field full">
                            <label>Email</label>
                            <input name="email" type="email" value={datosFormulario.email} onChange={manejarCambio} required />
                        </div>
                        <div className="input-field full">
                            <label>Contraseña Actual (Requerido para cambios)</label>
                            <input name="passwordActual" type="password" value={datosFormulario.passwordActual} onChange={manejarCambio} required />
                        </div>
                        <div className="input-field full">
                            <label>Nueva Contraseña (Opcional)</label>
                            <input name="contrasenia" type="password" value={datosFormulario.contrasenia} onChange={manejarCambio} />
                        </div>
                    </div>

                    {error && <div className="profile-error">{error}</div>}
                    {exito && <div className="profile-exito">{exito}</div>}

                    <button type="submit" className="save-btn" disabled={cargando}>
                        {cargando ? 'Guardando...' : 'Guardar Cambios'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default Profile;
