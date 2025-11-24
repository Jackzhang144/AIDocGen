import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '../api/client'

export const useHistoryStore = defineStore('history', () => {
  const history = ref([])
  const filters = ref({ keyword: '', language: '', source: '' })
  const pagination = ref({ page: 1, size: 10, total: 0 })

  async function fetchHistory() {
    const params = { ...filters.value, ...pagination.value }
    const data = await apiClient.fetchHistory(params)
    history.value = data.records || []
    pagination.value.total = data.total || 0
  }

  function setFilters(newFilters) {
    filters.value = { ...filters.value, ...newFilters }
    pagination.value.page = 1
    fetchHistory()
  }

  function nextPage() {
    const maxPage = Math.ceil((pagination.value.total || 0) / pagination.value.size)
    if (pagination.value.page < maxPage) {
      pagination.value.page += 1
      fetchHistory()
    }
  }

  function prevPage() {
    if (pagination.value.page > 1) {
      pagination.value.page -= 1
      fetchHistory()
    }
  }

  return { history, filters, pagination, fetchHistory, setFilters, nextPage, prevPage }
})
