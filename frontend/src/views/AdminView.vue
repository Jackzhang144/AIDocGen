<template>
  <div>
    <h1 class="text-2xl font-bold mb-4">{{ t('adminUserTitle') }}</h1>
    <AdminUsersTable />
  </div>
</template>

<script setup>
import AdminUsersTable from '../components/AdminUsersTable.vue';
import { useI18n } from '../i18n';
const { t } = useI18n();
</script>
