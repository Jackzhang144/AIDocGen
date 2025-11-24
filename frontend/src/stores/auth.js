import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import apiClient from '../api/client'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  const token = ref(localStorage.getItem('token'))
  const storedUser = localStorage.getItem('user')
  const user = ref(storedUser ? JSON.parse(storedUser) : null)

  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(newUser) {
    user.value = newUser
    localStorage.setItem('user', JSON.stringify(newUser))
  }

  async function login(credentials) {
    const data = await apiClient.login(credentials)
    setToken(data.token)
    setUser({
      username: data.username,
      email: data.email,
      role: data.role,
      apiQuota: data.apiQuota
    })
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push({ name: 'login' })
  }

  return { token, user, isAdmin, login, logout }
})
