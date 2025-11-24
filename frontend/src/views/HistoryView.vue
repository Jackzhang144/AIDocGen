<template>
  <div>
    <h1 class="text-2xl font-bold mb-4">{{ t('historyTitle') }}</h1>
    <HistoryList :is-page="true" />
  </div>
</template>

<script setup>
import HistoryList from '../components/HistoryList.vue';
import { useI18n } from '../i18n';
const { t } = useI18n();
</script>
