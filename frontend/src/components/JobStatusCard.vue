<template>
  <div v-if="job" class="p-6 mb-4 bg-white rounded-lg shadow-lg">
    <h3 class="text-xl font-bold mb-4 text-gray-800">{{ t('jobStatus') }}</h3>
    <div class="space-y-3">
      <p class="flex justify-between">
        <strong class="font-medium text-gray-600">{{ t('jobId') }}:</strong>
        <span class="text-gray-800">{{ job.id }}</span>
      </p>
      <p class="flex justify-between items-center">
        <strong class="font-medium text-gray-600">{{ t('jobState') }}:</strong>
        <span :class="statusClass" class="px-3 py-1 text-sm font-semibold rounded-full">
          {{ job.state }}
        </span>
      </p>
      <p v-if="job.state === 'FAILED'" class="flex justify-between">
        <strong class="font-medium text-gray-600">{{ t('jobReason') }}:</strong>
        <span class="text-red-600">{{ job.reason }}</span>
      </p>
      <div v-if="job.state === 'IN_PROGRESS'" class="flex items-center space-x-2 pt-2">
        <svg class="animate-spin h-5 w-5 text-indigo-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
        </svg>
        <p class="text-gray-600">{{ t('jobHint') }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useJobStore } from '../stores/job';
import { storeToRefs } from 'pinia';
import { useI18n } from '../i18n';

const jobStore = useJobStore();
const { currentJob: job } = storeToRefs(jobStore);
const { t } = useI18n();

const statusClass = computed(() => {
  switch (job.value?.state) {
    case 'SUCCEEDED':
      return 'bg-green-100 text-green-800';
    case 'FAILED':
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-yellow-100 text-yellow-800';
  }
});
</script>
