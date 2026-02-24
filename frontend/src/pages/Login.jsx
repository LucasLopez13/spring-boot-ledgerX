import React, { useState } from 'react';
import { authService } from '../services/api';
import './Login.css';

const Login = ({ alIniciarSesion, alMostrarRegistro }) => {
    const [correo, setCorreo] = useState('');
    const [contrasenia, setContrasenia] = useState('');
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const manejarEnvio = async (e) => {
        e.preventDefault();
        setError('');
        setCargando(true);

        try {
            await authService.login(correo, contrasenia);
            alIniciarSesion();
        } catch (err) {
            setError(err.response?.data || 'Credenciales incorrectas');
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="contenedor-login">
            <div className="tarjeta-login glass">
                <div className="cabecera-login">
                    <h1>Ledger<span>X</span></h1>
                    <p>Potenciando tus finanzas</p>
                </div>

                <form onSubmit={manejarEnvio}>
                    <div className="grupo-entrada">
                        <label>Email</label>
                        <input
                            type="email"
                            placeholder="Correo electrónico"
                            value={correo}
                            onChange={(e) => setCorreo(e.target.value)}
                            required
                        />
                    </div>

                    <div className="grupo-entrada">
                        <label>Contraseña</label>
                        <input
                            type="password"
                            placeholder="••••••••"
                            value={contrasenia}
                            onChange={(e) => setContrasenia(e.target.value)}
                            required
                        />
                    </div>

                    {error && <div className="msj-error">{error}</div>}

                    <button type="submit" className="boton-login" disabled={cargando}>
                        {cargando ? 'Iniciando...' : 'Entrar'}
                    </button>

                    <div className="invitacion-registro">
                        <p>¿No tienes cuenta?</p>
                        <button type="button" className="boton-secundario" onClick={alMostrarRegistro}>
                            Registrarse ahora
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Login;
