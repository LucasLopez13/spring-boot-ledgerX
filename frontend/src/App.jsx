import React, { useState, useEffect } from 'react';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import { serverService } from './services/api';
import './App.css';

function App() {
  const [haIniciadoSesion, setHaIniciadoSesion] = useState(false);
  const [vista, setVista] = useState('login'); // 'login', 'registro'

  const [serverStatus, setServerStatus] = useState('waking');

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setHaIniciadoSesion(true);
    }

    // Ping al servidor al cargar la web
    const despertarBackend = async () => {
      try {
        await serverService.wakeUpServer();
        setServerStatus('online');
      } catch (e) {
        setServerStatus('online');
      }
    };

    despertarBackend();
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
      {serverStatus === 'waking' && (
        <div className="server-status-banner status-sleeping">
          <div className="spinner-mini"></div>
          <span>Despertando servidor en la nube (puede demorar 30s)...</span>
        </div>
      )}
      {serverStatus === 'online' && (
        <div className="server-status-banner status-online">
          <span>🟢 Servidor Online - Conexión establecida</span>
        </div>
      )}

      {vista === 'login' ? (
        <Login alIniciarSesion={manejarInicioSesionExitoso} alMostrarRegistro={() => setVista('registro')} />
      ) : (
        <Register alVolver={() => setVista('login')} alRegistrar={() => setVista('login')} />
      )}
    </div>
  );
}

export default App;
