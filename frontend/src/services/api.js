import axios from 'axios';

const API_URL = 'http://localhost:8080';

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

export default api;
