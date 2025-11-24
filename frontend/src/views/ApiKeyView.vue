<template>
  <div>
    <h1 class="text-2xl font-bold mb-4">{{ t('adminApiKeyTitle') }}</h1>
    <AdminApiKeys />
  </div>
</template>

<script setup>
import AdminApiKeys from '../components/AdminApiKeys.vue';
import { useI18n } from '../i18n';
const { t } = useI18n();
</script>
