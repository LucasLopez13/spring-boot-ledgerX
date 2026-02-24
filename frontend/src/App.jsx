import React, { useState, useEffect } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import './App.css';

function App() {
  const [haIniciadoSesion, setHaIniciadoSesion] = useState(false);
  const [vista, setVista] = useState('login'); // 'login', 'registro'

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setHaIniciadoSesion(true);
    }
  }, []);

  const manejarInicioSesionExitoso = () => {
    setHaIniciadoSesion(true);
  };

  const manejarCierreSesion = () => {
    localStorage.removeItem('token');
    setHaIniciadoSesion(false);
    setVista('login');
  };

  if (haIniciadoSesion) {
    return <Dashboard alCerrarSesion={manejarCierreSesion} />;
  }

  return (
    <div className="contenedor-app">
      {vista === 'login' ? (
        <Login alIniciarSesion={manejarInicioSesionExitoso} alMostrarRegistro={() => setVista('registro')} />
      ) : (
        <Register alVolver={() => setVista('login')} alRegistrar={() => setVista('login')} />
      )}
    </div>
  );
}

export default App;
