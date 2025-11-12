<template>
  <section class="card auth-panel">
    <header>
      <h2>{{ mode === 'login' ? '登录账户' : '注册新用户' }}</h2>
      <p>
        {{ mode === 'login' ? '首次使用请注册，系统会自动为第一个用户授予管理员权限。' : '注册完成后立即获得访问令牌。' }}
      </p>
    </header>

    <form @submit.prevent="handleSubmit">
      <label>
        <span>用户名</span>
        <input v-model="form.username" required />
      </label>

      <label v-if="mode === 'register'">
        <span>邮箱</span>
        <input type="email" v-model="form.email" />
      </label>

      <label>
        <span>密码</span>
        <input type="password" v-model="form.password" required />
      </label>

      <button type="submit" :disabled="isSubmitting">
        {{ isSubmitting ? '提交中...' : mode === 'login' ? '登录' : '注册' }}
      </button>
      <p v-if="error" class="error">{{ error }}</p>
    </form>

    <footer>
      <button class="ghost" type="button" @click="toggleMode">
        {{ mode === 'login' ? '还没有账号？点击注册' : '已有账号？返回登录' }}
      </button>
    </footer>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { login, register } from '../api/client';
import { useAuthStore } from '../stores/auth';

const mode = ref('login');
const isSubmitting = ref(false);
const error = ref('');
const authStore = useAuthStore();

const form = reactive({
  username: '',
  email: '',
  password: ''
});

const toggleMode = () => {
  mode.value = mode.value === 'login' ? 'register' : 'login';
  error.value = '';
};

const handleSubmit = async () => {
  isSubmitting.value = true;
  error.value = '';
  try {
    const payload = { username: form.username.trim(), password: form.password };
    if (mode.value === 'register') {
      payload.email = form.email;
    }
    const request = mode.value === 'login' ? login : register;
    const { data } = await request(payload);
    authStore.setAuth(data.data);
  } catch (err) {
    error.value = err.message;
  } finally {
    isSubmitting.value = false;
  }
};
</script>

<style scoped>
.auth-panel {
  max-width: 480px;
  margin: 0 auto;
  text-align: left;
}

form {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

label span {
  display: inline-block;
  font-weight: 600;
  margin-bottom: 8px;
}

input {
  width: 100%;
  border: 1px solid #cbd5f5;
  border-radius: 12px;
  padding: 12px 16px;
  background: #f8fafc;
}

button {
  padding: 12px 20px;
  border-radius: 999px;
  border: none;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
}

.ghost {
  background: transparent;
  border: 1px solid #93c5fd;
  color: #1e3a8a;
}

.error {
  color: #dc2626;
}
</style>
