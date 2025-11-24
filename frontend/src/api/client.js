import axios from 'axios'
import { useAuthStore } from '../stores/auth'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
})

apiClient.interceptors.request.use(config => {
  const authStore = useAuthStore()
  const token = authStore.token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
    }
    const message = error.response?.data?.message || error.message
    return Promise.reject(new Error(message))
  }
)

const unwrap = (response) => response.data?.data ?? response.data

export default {
  async login(credentials) {
    const res = await apiClient.post('/auth/login', credentials)
    return unwrap(res)
  },
  async register(userInfo) {
    const res = await apiClient.post('/auth/register', userInfo)
    return unwrap(res)
  },
  async submitDocJob(payload) {
    const url = payload.isSelection ? '/docs/write/v3' : '/docs/write/v3/no-selection'
    const res = await apiClient.post(url, payload)
    return unwrap(res) // { id }
  },
  async fetchJobStatus(id) {
    const res = await apiClient.get(`/docs/worker/${id}`)
    return unwrap(res) // { id, state, reason, data }
  },
  async submitFeedback(payload) {
    const res = await apiClient.post('/docs/feedback', payload)
    return unwrap(res)
  },
  async fetchHistory(params) {
    const res = await apiClient.get('/docs/history', { params })
    return unwrap(res) // { total, page, size, records }
  },
  async fetchUsers() {
    const res = await apiClient.get('/admin/users')
    return unwrap(res)
  },
  async updateUser(id, payload) {
    const res = await apiClient.put(`/admin/users/${id}`, payload)
    return unwrap(res)
  },
  async listProviders() {
    const res = await apiClient.get('/admin/providers')
    return unwrap(res)
  },
  async saveProvider(payload) {
    const res = await apiClient.post('/admin/providers', payload)
    return unwrap(res)
  },
  async activateProvider(id) {
    const res = await apiClient.post(`/admin/providers/${id}/activate`)
    return unwrap(res)
  },
  async document(payload) {
    const res = await apiClient.post('/v1/document', payload)
    return unwrap(res)
  },
  async listLanguages() {
    const res = await apiClient.get('/v1/list/languages')
    return unwrap(res)
  },
  async listFormats() {
    const res = await apiClient.get('/v1/list/formats')
    return unwrap(res)
  }
}
