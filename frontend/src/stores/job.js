import { defineStore } from 'pinia'
import { ref } from 'vue'
import apiClient from '../api/client'

export const useJobStore = defineStore('job', () => {
  const currentJob = ref(null)
  const pollInterval = ref(null)

  async function submitJob(payload) {
    const data = await apiClient.submitDocJob(payload)
    currentJob.value = { id: data.id, state: 'PENDING' }
    startPolling()
  }

  async function pollJobStatus() {
    if (!currentJob.value) return

    try {
      const data = await apiClient.fetchJobStatus(currentJob.value.id)
      currentJob.value = {
        id: data.id,
        state: data.state,
        reason: data.reason,
        result: data.data
      }

      if (['SUCCEEDED', 'FAILED'].includes(data.state)) {
        stopPolling()
      }
    } catch (error) {
      console.error('Error polling job status:', error)
      currentJob.value.state = 'FAILED'
      currentJob.value.reason = 'Failed to fetch job status.'
      stopPolling()
    }
  }

  function startPolling() {
    if (pollInterval.value) return
    pollInterval.value = setInterval(pollJobStatus, 2000)
  }

  function stopPolling() {
    clearInterval(pollInterval.value)
    pollInterval.value = null
  }

  return { currentJob, submitJob, stopPolling }
})
