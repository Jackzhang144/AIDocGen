<template>
  <div class="p-8 bg-white rounded-lg shadow-lg">
    <h2 class="text-2xl font-bold mb-6 text-gray-800">{{ t('generatorTitle') }}</h2>
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <div>
        <label for="code" class="block text-sm font-medium text-gray-700">{{ t('codeSnippet') }}</label>
        <textarea v-model="form.code" id="code" rows="10" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out"></textarea>
      </div>
      <div>
        <label for="context" class="block text-sm font-medium text-gray-700">{{ t('context') }}</label>
        <textarea v-model="form.context" id="context" rows="5" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out"></textarea>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label for="language" class="block text-sm font-medium text-gray-700">{{ t('language') }}</label>
          <select v-model="form.languageId" id="language" required class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out">
            <option v-for="lang in languages" :key="lang" :value="lang">{{ lang }}</option>
          </select>
        </div>
        <div>
          <label for="format" class="block text-sm font-medium text-gray-700">{{ t('docFormat') }}</label>
          <select v-model="form.docStyle" id="format" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out">
            <option value="Auto">Auto</option>
            <option v-for="fmt in formats" :key="fmt.id" :value="fmt.id">{{ fmt.id }}</option>
          </select>
        </div>
      </div>

      <fieldset>
        <legend class="text-base font-medium text-gray-900">Options</legend>
        <div class="mt-4 space-y-4">
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="form.commented" id="commented" type="checkbox" class="h-4 w-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500">
            </div>
            <div class="ml-3 text-sm">
              <label for="commented" class="font-medium text-gray-700">{{ t('commented') }}</label>
            </div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center h-5">
              <input v-model="form.isSelection" id="isSelection" type="checkbox" class="h-4 w-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500">
            </div>
            <div class="ml-3 text-sm">
              <label for="isSelection" class="font-medium text-gray-700">{{ t('selectionMode') }}</label>
            </div>
          </div>
        </div>
      </fieldset>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label for="width" class="block text-sm font-medium text-gray-700">{{ t('maxWidth') }}</label>
          <input v-model.number="form.width" id="width" type="number" min="20" max="160" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out">
        </div>
        <div>
          <label for="lineCommentRatio" class="block text-sm font-medium text-gray-700">Line Comment Ratio</label>
          <input v-model.number="form.lineCommentRatio" type="number" min="0" max="1" step="0.1" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out" />
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-gray-700">Mode</label>
          <select v-model="form.mode" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out">
            <option value="inserted">Inserted Code</option>
            <option value="text">Text Only</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700">Quality</label>
          <select v-model="form.quality" class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500 transition duration-150 ease-in-out">
            <option value="fast">Fast</option>
            <option value="balanced">Balanced</option>
            <option value="deep">Deep</option>
          </select>
        </div>
      </div>

      <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
      
      <div>
        <button type="submit" :disabled="isLoading" class="w-full flex justify-center py-3 px-4 border border-transparent rounded-full shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 transition duration-150 ease-in-out">
          {{ isLoading ? t('submitting') : t('submit') }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useJobStore } from '../stores/job';
import apiClient from '../api/client';
import { useI18n } from '../i18n';

const jobStore = useJobStore();
const { t } = useI18n();
const form = reactive({
  code: '',
  context: '',
  languageId: 'python',
  docStyle: 'Auto',
  width: 80,
  commented: true,
  isSelection: true,
  source: 'web',
  mode: 'inserted',
  quality: 'balanced',
  lineCommentRatio: 0.3
});
const languages = ref([]);
const formats = ref([]);
const error = ref(null);
const isLoading = ref(false);

onMounted(async () => {
  try {
    const [langRes, formatRes] = await Promise.all([
      apiClient.listLanguages(),
      apiClient.listFormats()
    ]);
    languages.value = langRes.languages ?? langRes;
    formats.value = formatRes.formats ?? formatRes;
  } catch (e) {
    error.value = t('errorLoadMeta');
  }
});

async function handleSubmit() {
  if (!form.code && !form.context) {
    error.value = t('errorRequireCode');
    return;
  }
  error.value = null;
  isLoading.value = true;
  try {
    await jobStore.submitJob({
      code: form.code || undefined,
      context: form.context || undefined,
      languageId: form.languageId,
      commented: form.commented,
      docStyle: form.docStyle,
      width: form.width,
      isSelection: form.isSelection,
      source: form.source,
      mode: form.mode,
      quality: form.quality,
      lineCommentRatio: form.lineCommentRatio
    });
  } catch (e) {
    error.value = e.message || t('loadFailed');
  } finally {
    isLoading.value = false;
  }
}
</script>
