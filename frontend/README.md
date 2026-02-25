# 💻 LedgerX - Frontend / Interfaz Web

Esta es la aplicación orientada al usuario para el proyecto LedgerX, construida con React y empaquetada con Vite. Ofrece un diseño moderno estilo "Glassmorphism" con estética premium, colores en tonos *"Teal"* oscuros y es totalmente responsivo (adaptable a móviles, tablets y navegadores de escritorio web).

## 🛠️ Tecnologías Utilizadas
- **React 18** (Librería principal UI)
- **Vite** (Herramienta de compilación ultrarrápida y servidor de desarrollo)
- **CSS Vanilla** (Hojas de estilo modernas, variables, CSS Grid / Flexbox)
- **Context API & Hooks** (Para manejo local de estados / interacciones asíncronas)

## 📐 Características
- **Autenticación Completa (Login / Registro):** Interfaces dedicadas para la creación de usuarios o validación de credenciales con conexión protegida al backend (Tokens JWT).
- **Dashboard Minimalista y Elegante:** Vista principal donde es posible observar saldos, consultar CBU virtual y ejecutar depósitos, retiros o transferencias ágilmente.
- **Bono de Bienvenida Inteligente:** Un botón especial con interfaz destacada que solo se renderiza si la cuenta es totalmente nueva (saldo 0 y sin historial), permitiendo inyectar un saldo inicial a la billetera mediante una sola llamada a la API.
- **Indicador de Servidor en Frío (Cold Start):** Un banner dinámico estilo *Glassmorphism* que se comunica asíncronamente con el backend al cargar la aplicación, garantizando una excelente experiencia de usuario (UX) informando al visitante mientras los servidores en la nube "despiertan" tras la inactividad.
- **Historial Deslizable con Paginación:** Tabla de transacciones fluida, adaptada inteligentemente para móviles (desplazamiento lateral). Incluye controles de **"Anterior" y "Siguiente"** (paginación del lado del servidor) para no sobrecargar la vista con historiales extensos.
- **Respuestas Visuales al Usuario:** Skeleton Loaders para tiempos de inicialización y alertas amigables al fallar u originar transacciones.

## 🚀 Compilar y Levantar Localmente

### Prerrequisitos
- **Node.js** (versión recomendada 18+) y NPM instalados.

### Instalación Guiada
1. Ubicarse en el directorio e instalar dependencias vía consola:
   ```bash
   cd frontend
   npm install
   ```
2. Crear un archivo `.env` en la raíz de la carpeta `frontend` si tienes un puerto modificado. Por defecto apunta a localhost, pero en caso necesario:
   ```env
   VITE_API_URL=http://localhost:8080
   ```
3. Ejecutar y renderizar el entorno de desarrollo local:
   ```bash
   npm run dev
   ```

El servidor Vite desplegará por defecto tu aplicación velozmente en `http://localhost:5173`.

## 🌐 Configuración de Producción
El archivo `index.html` cuenta con optimizaciones nativas. 
Si decides hacer "Deploy" de este sitio a hostings gratuitos como **Vercel**, cerciórate de indicarle en "Vercel Environment Variables" que la clave `VITE_API_URL` apunte a la nube viva de tu backend Spring Boot.
