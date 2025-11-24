<template>
  <div class="p-6 bg-white rounded-lg shadow-lg space-y-4">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-gray-800">{{ t('historyTitle') }}</h2>
        <p class="text-sm text-gray-500">{{ t('historyFilters') }}</p>
      </div>
      <button @click="refresh" class="px-3 py-1 text-sm rounded border">{{ t('historyRefresh') }}</button>
    </div>

    <div class="grid grid-cols-1 md:grid-cols-4 gap-3">
      <input v-model="filters.keyword" :placeholder="t('historyKeyword')" class="border rounded px-3 py-2" />
      <input v-model="filters.language" :placeholder="t('historyLanguage')" class="border rounded px-3 py-2" />
      <input v-model="filters.source" :placeholder="t('historySource')" class="border rounded px-3 py-2" />
      <button @click="applyFilters" class="px-3 py-2 bg-indigo-600 text-white rounded">{{ t('historyFilters') }}</button>
    </div>

    <div v-if="history.length === 0" class="text-center py-10">
      <p class="text-gray-500 mb-4">{{ t('historyEmpty') }}</p>
      <router-link to="/" class="inline-block px-6 py-3 text-sm font-medium text-white bg-indigo-600 rounded-full hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition duration-150 ease-in-out">
        {{ t('historyCTA') }}
      </router-link>
    </div>

    <ul v-else class="space-y-4">
      <li v-for="item in history" :key="item.id"
          class="border rounded-lg overflow-hidden transition-shadow duration-200 hover:shadow-md"
          @click="toggle(item.id)">

        <div class="p-4 cursor-pointer">
          <div class="flex justify-between items-center">
            <p class="text-sm text-gray-500">{{ new Date(item.timestamp).toLocaleString() }}</p>
            <span class="text-xs px-2 py-1 rounded-full"
                  :class="item.feedback === 1 ? 'bg-green-100 text-green-800' : item.feedback === -1 ? 'bg-red-100 text-red-800' : 'bg-gray-100 text-gray-800'">
              {{ item.feedback === 1 ? '👍' : item.feedback === -1 ? '👎' : 'N/A' }}
            </span>
          </div>
          <p class="text-sm text-gray-600 mt-1">{{ item.language }} · {{ item.source }} · {{ item.modelProvider || 'unknown' }}</p>
          <p class="mt-2 font-mono bg-gray-50 p-3 rounded-md text-sm truncate">{{ item.outputPreview }}</p>
        </div>

        <div v-if="expandedId === item.id" class="px-4 pb-4">
          <div class="border-t pt-4 mt-4 space-y-4">
            <div>
              <h4 class="text-xs font-semibold text-gray-500 uppercase mb-2">Details</h4>
              <p class="text-sm text-gray-700">
                {{ t('language') }}: <span class="font-medium">{{ item.language }}</span> |
                {{ t('model') }}: <span class="font-medium">{{ item.modelProvider || 'unknown' }}</span> |
                {{ t('source') }}: <span class="font-medium">{{ item.source }}</span>
              </p>
            </div>
            <div>
              <h4 class="text-xs font-semibold text-gray-500 uppercase mb-2">Output</h4>
              <pre class="whitespace-pre-wrap text-sm bg-gray-50 p-3 rounded-md">{{ item.outputPreview }}</pre>
            </div>
            <div>
              <h4 class="text-xs font-semibold text-gray-500 uppercase mb-2">Prompt</h4>
              <pre class="whitespace-pre-wrap text-sm bg-gray-50 p-3 rounded-md">{{ item.promptPreview }}</pre>
            </div>
            <button class="mt-4 px-4 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-full hover:bg-gray-300" @click.stop="toggle(null)">
              {{ t('close') }}
            </button>
          </div>
        </div>
      </li>
    </ul>

    <div class="flex items-center justify-between" v-if="history.length">
      <button class="px-3 py-1 border rounded" :disabled="pagination.page <= 1" @click.stop="prevPage">Prev</button>
      <span class="text-sm text-gray-600">Page {{ pagination.page }} / {{ totalPages }}</span>
      <button class="px-3 py-1 border rounded" :disabled="pagination.page >= totalPages" @click.stop="nextPage">Next</button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import { useHistoryStore } from '../stores/history';
import { storeToRefs } from 'pinia';
import { useI18n } from '../i18n';

const props = defineProps({
  isPage: {
    type: Boolean,
    default: false
  }
});

const historyStore = useHistoryStore();
const { history, filters, pagination } = storeToRefs(historyStore);
const { t } = useI18n();
const expandedId = ref(null);
const totalPages = computed(() => {
  if (!pagination.value.total) return 1;
  return Math.max(1, Math.ceil(pagination.value.total / pagination.value.size));
});

const toggle = (id) => {
  expandedId.value = expandedId.value === id ? null : id;
};

onMounted(() => {
  historyStore.fetchHistory();
});

const applyFilters = () => {
  pagination.value.page = 1;
  historyStore.fetchHistory();
};

const refresh = () => {
  historyStore.fetchHistory();
};

const prevPage = () => {
  historyStore.prevPage();
};

const nextPage = () => {
  historyStore.nextPage();
};
</script>
