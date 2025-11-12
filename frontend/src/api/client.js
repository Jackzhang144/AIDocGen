import axios from 'axios';

const defaultBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

const TOKEN_KEY = 'aidoc_token';

const httpClient = axios.create({
  baseURL: defaultBaseUrl,
  timeout: Number(import.meta.env.VITE_HTTP_TIMEOUT_MS ?? 20000)
});

httpClient.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

httpClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const message = error.response?.data?.message ?? error.message;
    return Promise.reject(new Error(message));
  }
);

export const submitDocJob = (payload) => httpClient.post('/docs/write/v3', payload);

export const fetchJobStatus = (jobId) => httpClient.get(`/docs/worker/${jobId}`);

export const submitFeedback = (feedbackPayload) => httpClient.post('/docs/feedback', feedbackPayload);

export const login = (payload) => httpClient.post('/auth/login', payload);

export const register = (payload) => httpClient.post('/auth/register', payload);

export const fetchHistory = (params) => httpClient.get('/docs/history', { params });

export const fetchUsers = () => httpClient.get('/admin/users');

export const updateUser = (id, payload) => httpClient.put(`/admin/users/${id}`, payload);

export const listApiKeys = () => httpClient.get('/admin/api-keys');

export const createApiKey = (payload) => httpClient.post('/admin/api-keys', payload);

export const deleteApiKey = (id) => httpClient.delete(`/admin/api-keys/${id}`);
