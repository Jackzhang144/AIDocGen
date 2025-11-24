<template>
  <div class="space-y-4">
    <h1 class="text-2xl font-bold mb-4">{{ t('settings') }}</h1>
    <p>User: {{ authStore.user?.email }}</p>
    <div class="space-y-2">
      <p class="text-sm text-gray-600">{{ t('languageToggleLabel') }}</p>
      <div class="flex gap-2">
        <button @click="setLang('zh')" class="px-3 py-1 border rounded" :class="{'bg-gray-200': lang==='zh'}">中文</button>
        <button @click="setLang('en')" class="px-3 py-1 border rounded" :class="{'bg-gray-200': lang==='en'}">English</button>
      </div>
    </div>
    <div class="space-y-2">
      <p class="text-sm text-gray-600">{{ t('theme') }}</p>
      <div class="flex gap-2">
        <button @click="setTheme('light')" class="px-3 py-1 border rounded" :class="{'bg-gray-200': theme==='light'}">{{ t('light') }}</button>
        <button @click="setTheme('dark')" class="px-3 py-1 border rounded" :class="{'bg-gray-200': theme==='dark'}">{{ t('dark') }}</button>
        <button @click="setTheme('system')" class="px-3 py-1 border rounded" :class="{'bg-gray-200': theme==='system'}">{{ t('system') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useAuthStore } from '../stores/auth';
import { useI18n } from '../i18n';
import { ref, watch } from 'vue';

const authStore = useAuthStore();
const { t, setLang, lang } = useI18n();
const theme = ref(localStorage.getItem('theme') || 'system');

const applyTheme = () => {
  const root = document.documentElement;
  if (theme.value === 'dark') {
    root.classList.add('dark');
  } else if (theme.value === 'light') {
    root.classList.remove('dark');
  } else {
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    root.classList.toggle('dark', prefersDark);
  }
};

watch(theme, (val) => {
  localStorage.setItem('theme', val);
  applyTheme();
}, { immediate: true });

function setTheme(val) {
  theme.value = val;
}
</script>
