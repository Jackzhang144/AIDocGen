<template>
  <div class="bg-white rounded-lg shadow-md">
    <div class="p-4 flex flex-wrap gap-2 justify-between items-center">
        <h3 class="text-lg font-bold">模型提供方配置</h3>
        <div class="flex gap-2">
          <button @click="fetchConfigs" class="px-3 py-1 font-medium border rounded-md">{{ t('refresh') }}</button>
          <button @click="openModal()" class="px-4 py-2 font-medium text-white bg-indigo-600 rounded-full hover:bg-indigo-700">新增/编辑</button>
        </div>
    </div>
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Provider</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Model</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Base URL</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Temp</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Max Tokens</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Enabled</th>
          <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('actions') }}</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <tr v-for="cfg in configs" :key="cfg.id">
          <td class="px-6 py-4 whitespace-nowrap font-mono">{{ cfg.provider }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ cfg.model }}</td>
          <td class="px-6 py-4 whitespace-nowrap truncate max-w-xs" :title="cfg.baseUrl">{{ cfg.baseUrl }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ cfg.temperature }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ cfg.maxOutputTokens }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ cfg.enabled ? 'Yes' : 'No' }}</td>
          <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
            <button @click="openModal(cfg)" class="text-indigo-600 hover:text-indigo-900">{{ t('edit') }}</button>
            <button v-if="!cfg.enabled" @click="activate(cfg.id)" class="text-green-600 hover:text-green-900">Activate</button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="showModal" class="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-lg w-full max-w-lg p-6 space-y-4">
        <h4 class="text-lg font-bold">{{ current.id ? t('edit') : '新增配置' }}</h4>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700">Provider</label>
            <select v-model="current.provider" class="mt-1 w-full border rounded px-3 py-2">
              <option value="openai">openai</option>
              <option value="deepseek">deepseek</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">API Key</label>
            <input v-model="current.apiKey" type="text" class="mt-1 w-full border rounded px-3 py-2" placeholder="sk-..." />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Base URL</label>
            <input v-model="current.baseUrl" type="text" class="mt-1 w-full border rounded px-3 py-2" placeholder="https://api.openai.com/v1" />
          </div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label class="block text-sm font-medium text-gray-700">Model</label>
              <input v-model="current.model" type="text" class="mt-1 w-full border rounded px-3 py-2" placeholder="gpt-4o-mini" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700">Temperature</label>
              <input v-model.number="current.temperature" type="number" step="0.01" class="mt-1 w-full border rounded px-3 py-2" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">Max Output Tokens</label>
            <input v-model.number="current.maxOutputTokens" type="number" class="mt-1 w-full border rounded px-3 py-2" />
          </div>
        </div>
        <div class="flex justify-end gap-2">
          <button @click="closeModal" class="px-4 py-2 border rounded">{{ t('cancel') }}</button>
          <button @click="save" :disabled="saving" class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:opacity-60">{{ saving ? '...' : t('save') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from '../api/client';
import { useI18n } from '../i18n';

const configs = ref([]);
const { t } = useI18n();
const showModal = ref(false);
const saving = ref(false);
const current = ref({
  id: null,
  provider: 'openai',
  apiKey: '',
  baseUrl: '',
  model: '',
  temperature: 0.2,
  maxOutputTokens: 512
});

async function fetchConfigs() {
  try {
    configs.value = await apiClient.listProviders();
  } catch (error) {
    console.error('Failed to fetch provider configs:', error);
  }
}

function openModal(cfg) {
  showModal.value = true;
  if (cfg) {
    current.value = { ...cfg, apiKey: '' };
  } else {
    current.value = {
      id: null,
      provider: 'openai',
      apiKey: '',
      baseUrl: '',
      model: '',
      temperature: 0.2,
      maxOutputTokens: 512
    };
  }
}

function closeModal() {
  showModal.value = false;
}

async function save() {
  saving.value = true;
  try {
    await apiClient.saveProvider(current.value);
    await fetchConfigs();
    closeModal();
  } catch (error) {
    console.error('Failed to save provider:', error);
  } finally {
    saving.value = false;
  }
}

async function activate(id) {
  try {
    await apiClient.activateProvider(id);
    await fetchConfigs();
  } catch (error) {
    console.error('Failed to activate provider:', error);
  }
}

onMounted(fetchConfigs);
</script>
