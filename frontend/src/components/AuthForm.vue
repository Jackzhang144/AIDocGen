<template>
  <div class="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md">
    <h1 class="text-2xl font-bold text-center">{{ isLogin ? t('login') : t('register') }}</h1>
    <form @submit.prevent="handleSubmit" class="space-y-6">
      <div>
        <label for="username" class="text-sm font-medium text-gray-700">{{ t('username') }}</label>
        <input v-model="form.username" id="username" type="text" required class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500">
      </div>
      <div>
        <label for="email" class="text-sm font-medium text-gray-700">{{ t('email') }}</label>
        <input v-model="form.email" id="email" type="email" required class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500">
      </div>
      <div>
        <label for="password" class="text-sm font-medium text-gray-700">{{ t('password') }}</label>
        <input v-model="form.password" id="password" type="password" required class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500">
      </div>
      <div v-if="!isLogin">
        <label for="confirmPassword" class="text-sm font-medium text-gray-700">{{ t('confirmPassword') }}</label>
        <input v-model="form.confirmPassword" id="confirmPassword" type="password" required class="w-full px-3 py-2 mt-1 border border-gray-300 rounded-md shadow-sm focus:ring-indigo-500 focus:border-indigo-500">
      </div>
      <p v-if="error" class="text-red-500 text-sm">{{ error }}</p>
      <div>
        <button type="submit" class="w-full px-4 py-2 font-medium text-white bg-indigo-600 rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500">
          {{ isLogin ? t('login') : t('register') }}
        </button>
      </div>
    </form>
    <p class="text-sm text-center">
      <router-link :to="isLogin ? '/register' : '/login'" class="font-medium text-indigo-600 hover:text-indigo-500">
        {{ isLogin ? t('needAccount') : t('alreadyAccount') }}
      </router-link>
    </p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import apiClient from '../api/client';
import { useI18n } from '../i18n';

const props = defineProps({
  isLogin: {
    type: Boolean,
    default: true
  }
});

const router = useRouter();
const authStore = useAuthStore();
const { t } = useI18n();
const form = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});
const error = ref(null);

async function handleSubmit() {
  error.value = null;
  if (props.isLogin) {
    try {
      await authStore.login({ username: form.username, password: form.password });
      router.push('/');
    } catch (e) {
      error.value = e.message || t('loginFailed');
    }
  } else {
    if (form.password !== form.confirmPassword) {
      error.value = t('passwordMismatch');
      return;
    }
    try {
      await apiClient.register({ username: form.username, email: form.email, password: form.password });
      router.push('/login');
    } catch (e) {
      error.value = e.message || t('registerFailed');
    }
  }
}
</script>
