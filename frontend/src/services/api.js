import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const api = axios.create({
    baseURL: API_URL,
});

// Interceptor para agregar el token JWT a todas las peticiones
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export const authService = {
    login: async (email, password) => {
        const response = await api.post('/login', { email, contrasenia: password });
        if (response.data.tokenJWT) {
            localStorage.setItem('token', response.data.tokenJWT);
        }
        return response.data;
    },
    logout: () => {
        localStorage.removeItem('token');
    }
};

export const serverService = {
    wakeUpServer: async () => {
        try {
            // Hacemos ping a la ruta pública de Swagger para despertar al server
            const response = await api.get('/v3/api-docs', { timeout: 60000 });
            return response.status === 200;
        } catch (error) {
            return false;
        }
    }
};

export default api;
