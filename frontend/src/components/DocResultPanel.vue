<template>
  <div v-if="job && job.state === 'SUCCEEDED'" class="p-6 bg-white rounded-lg shadow-lg mt-4">
    <h3 class="text-xl font-bold mb-4 text-gray-800">{{ t('jobResult') }}</h3>
    
    <div class="border-b border-gray-200">
      <nav class="-mb-px flex space-x-4" aria-label="Tabs">
        <button @click="activeTab='annotated'" :class="tabClass('annotated')">Annotated</button>
        <button @click="activeTab='comment'" :class="tabClass('comment')">Comment</button>
      </nav>
    </div>

    <div class="mt-4 bg-gray-50 p-4 rounded-md overflow-x-auto space-y-3">
      <div>
        <h4 class="text-sm font-semibold mb-1">{{ activeTab==='annotated' ? 'Annotated' : 'Comment' }}</h4>
        <pre class="text-sm"><code>{{ activeTab==='annotated' ? (job.result?.annotatedCode || job.result?.documentation) : (job.result?.documentation || job.result?.rawComment) }}</code></pre>
      </div>
      <div v-if="job.result?.preview && activeTab==='annotated'">
        <h4 class="text-sm font-semibold mb-1">{{ t('preview') }}</h4>
        <pre class="text-sm"><code>{{ job.result.preview }}</code></pre>
      </div>
    </div>

    <div class="mt-4 flex flex-wrap items-center justify-between">
      <div class="flex items-center space-x-2">
        <button @click="copyToClipboard(activeTab==='annotated' ? (job.result?.annotatedCode || job.result?.documentation) : (job.result?.documentation || job.result?.rawComment))" class="px-4 py-2 text-sm font-medium text-white bg-indigo-600 rounded-full hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out">
          {{ t('copy') }}
        </button>
        <div v-if="job.result?.feedbackId && !feedbackMessage" class="flex items-center space-x-1">
          <button @click="submitFeedback(1)" class="p-2 rounded-full hover:bg-gray-200 transition duration-150 ease-in-out text-lg">👍</button>
          <button @click="submitFeedback(-1)" class="p-2 rounded-full hover:bg-gray-200 transition duration-150 ease-in-out text-lg">👎</button>
        </div>
      </div>
      <div class="mt-2 md:mt-0 text-xs text-gray-500">
        Provider: {{ job.result?.modelProvider || 'n/a' }} · {{ job.result?.docFormat }} / {{ job.result?.commentFormat }} · {{ job.result?.inferenceLatencyMs }}ms
      </div>
    </div>
    
    <div v-if="feedbackMessage" class="mt-3 text-sm font-medium text-green-600">{{ feedbackMessage }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useJobStore } from '../stores/job';
import { storeToRefs } from 'pinia';
import apiClient from '../api/client';
import { useI18n } from '../i18n';

const jobStore = useJobStore();
const { currentJob: job } = storeToRefs(jobStore);
const feedbackMessage = ref('');
const { t } = useI18n();
const activeTab = ref('annotated');

function tabClass(tab) {
  const baseClasses = 'px-3 py-2 font-medium text-sm rounded-t-md focus:outline-none';
  if (activeTab.value === tab) {
    return `${baseClasses} border-b-2 border-indigo-500 text-indigo-600`;
  }
  return `${baseClasses} text-gray-500 hover:text-gray-700 hover:border-gray-300`;
}

function copyToClipboard(content) {
  if (content) {
    navigator.clipboard.writeText(content);
  }
}

async function submitFeedback(feedback) {
  try {
    await apiClient.submitFeedback({
      id: job.value.result.feedbackId,
      feedback: feedback,
    });
    feedbackMessage.value = t('feedbackThanks');
  } catch (error) {
    feedbackMessage.value = t('feedbackFailed');
  }
}
</script>
