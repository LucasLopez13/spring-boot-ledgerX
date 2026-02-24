import api from './api';

export const walletService = {
    getSaldo: async () => {
        const response = await api.get('/billeteras/saldo');
        return response.data;
    },

    getHistorial: async (page = 0, size = 10) => {
        const response = await api.get(`/transacciones?page=${page}&size=${size}`);
        return response.data;
    },

    reclamarBono: async () => {
        const response = await api.post('/transacciones/bono');
        return response.data;
    }
};

export const transactionService = {
    realizarTransaccion: async (datos) => {
        const response = await api.post('/transacciones', datos);
        return response.data;
    }
};
