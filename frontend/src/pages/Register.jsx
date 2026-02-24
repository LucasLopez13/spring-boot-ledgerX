import React, { useState } from 'react';
import './Login.css';

const Register = ({ alVolver, alRegistrar }) => {
    const [datosRegistro, setDatosRegistro] = useState({
        nombre: '',
        apellido: '',
        email: '',
        contrasenia: ''
    });
    const [error, setError] = useState('');
    const [cargando, setCargando] = useState(false);

    const manejarCambio = (e) => {
        setDatosRegistro({ ...datosRegistro, [e.target.name]: e.target.value });
    };

    const manejarEnvio = async (e) => {
        e.preventDefault();
        setError('');
        setCargando(true);

        try {
            const response = await fetch('http://localhost:8080/usuarios/registrar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(datosRegistro)
            });

            const data = await response.json();

            if (!response.ok) {
                // Manejamos tanto hilos de texto como arrays de validación (GestorDeErrores)
                let msg = 'Error al registrar usuario';
                if (typeof data === 'string') {
                    msg = data;
                } else if (Array.isArray(data)) {
                    msg = data[0].error;
                }
                throw new Error(msg);
            }

            alert('¡Cuenta creada con éxito! Ya puedes iniciar sesión.');
            alRegistrar();
        } catch (err) {
            setError(err.message || 'Error al registrar usuario');
        } finally {
            setCargando(false);
        }
    };

    return (
        <div className="contenedor-login">
            <div className="tarjeta-login glass">
                <div className="cabecera-login">
                    <h1>Ledger<span>X</span></h1>
                    <p>Crea tu cuenta gratuita</p>
                </div>

                <form onSubmit={manejarEnvio}>
                    <p className="leyenda-obligatoria">* Campos obligatorios</p>
                    <div className="fila-entrada">
                        <div className="grupo-entrada">
                            <label>Nombre <span>*</span></label>
                            <input name="nombre" value={datosRegistro.nombre} onChange={manejarCambio} required />
                        </div>
                        <div className="grupo-entrada">
                            <label>Apellido <span>*</span></label>
                            <input name="apellido" value={datosRegistro.apellido} onChange={manejarCambio} required />
                        </div>
                    </div>

                    <div className="grupo-entrada">
                        <label>Email <span>*</span></label>
                        <input name="email" type="email" value={datosRegistro.email} onChange={manejarCambio} required />
                    </div>

                    <div className="grupo-entrada">
                        <label>Contraseña <span>*</span></label>
                        <input name="contrasenia" type="password" value={datosRegistro.contrasenia} onChange={manejarCambio} required />
                    </div>

                    {error && <div className="msj-error">{error}</div>}

                    <button type="submit" className="boton-login" disabled={cargando}>
                        {cargando ? 'Registrando...' : 'Crear Cuenta'}
                    </button>

                    <div className="invitacion-registro">
                        <button type="button" className="boton-secundario" onClick={alVolver}>
                            &larr; Volver al Login
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Register;
