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
  const [isTakingLong, setIsTakingLong] = useState(false);

  useEffect(() => {
    let isMounted = true;
    const token = localStorage.getItem('token');
    if (token) {
      setHaIniciadoSesion(true);
    }

    const timer = setTimeout(() => {
      if (isMounted) setIsTakingLong(true);
    }, 30000);

    // Ping iterativo al servidor al cargar la web
    const despertarBackend = async () => {
      let isOnline = false;
      while (!isOnline && isMounted) {
        isOnline = await serverService.wakeUpServer();

        if (isOnline && isMounted) {
          setServerStatus('online');
        } else if (isMounted) {
          // Si falla (timeout u otro error), esperamos 3 segundos y volvemos a intentar
          await new Promise(resolve => setTimeout(resolve, 3000));
        }
      }
    };

    despertarBackend();

    return () => {
      isMounted = false;
      clearTimeout(timer);
    };
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
          <span>{isTakingLong ? "Falta muy poco, el servidor está ajustando los últimos detalles..." : "Despertando servidor en la nube (puede demorar 30s)..."}</span>
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
