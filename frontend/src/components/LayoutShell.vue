<template>
  <div class="min-h-screen bg-gray-100 dark:bg-gray-900 text-gray-900 dark:text-gray-100">
    <nav class="bg-white dark:bg-gray-800 shadow-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex items-center justify-between h-16">
          <div class="flex items-center">
            <div class="flex-shrink-0">
              <router-link to="/" class="text-2xl font-bold">{{ t('brand') }}</router-link>
            </div>
            <div class="hidden md:block">
              <div class="ml-10 flex items-baseline space-x-4">
                <router-link to="/" class="px-3 py-2 rounded-md text-sm font-medium">{{ t('navGenerator') }}</router-link>
                <router-link to="/history" class="px-3 py-2 rounded-md text-sm font-medium">{{ t('navHistory') }}</router-link>
                <router-link v-if="authStore.isAdmin" to="/admin" class="px-3 py-2 rounded-md text-sm font-medium">{{ t('navAdmin') }}</router-link>
                <router-link v-if="authStore.isAdmin" to="/api-keys" class="px-3 py-2 rounded-md text-sm font-medium">{{ t('navApiKeys') }}</router-link>
              </div>
            </div>
          </div>
          <div class="hidden md:block">
            <div class="ml-4 flex items-center md:ml-6">
              <button @click="toggleLang" class="px-3 py-1 text-sm rounded-md border border-gray-300 dark:border-gray-600 mr-3">
                {{ t('navToggleLang') }}
              </button>
              <!-- Profile dropdown -->
              <div class="ml-3 relative">
                <div>
                  <button @click="menuOpen = !menuOpen" class="max-w-xs bg-gray-800 rounded-full flex items-center text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-offset-gray-800 focus:ring-white">
                    <span class="sr-only">Open user menu</span>
                    <img class="h-8 w-8 rounded-full" src="https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=facearea&facepad=2&w=256&h=256&q=80" alt="">
                  </button>
                </div>
                <div v-if="menuOpen" class="origin-top-right absolute right-0 mt-2 w-48 rounded-md shadow-lg py-1 bg-white ring-1 ring-black ring-opacity-5">
                  <p class="block px-4 py-2 text-sm text-gray-700">{{ authStore.user?.email }}</p>
                  <router-link to="/settings" class="block px-4 py-2 text-sm text-gray-700">{{ t('navSettings') }}</router-link>
                  <a href="#" @click.prevent="authStore.logout()" class="block px-4 py-2 text-sm text-gray-700">{{ t('navLogout') }}</a>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>

    <main>
      <div class="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useAuthStore } from '../stores/auth';
import { useI18n } from '../i18n';
const authStore = useAuthStore();
const menuOpen = ref(false);
const { t, lang, setLang } = useI18n();

const toggleLang = () => {
  setLang(lang.value === 'zh' ? 'en' : 'zh')
};
</script>
