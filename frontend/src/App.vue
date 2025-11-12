<template>
  <div class="page">
    <header class="hero">
      <div>
        <p class="eyebrow">AiDoc</p>
        <h1>代码文档生成器</h1>
        <p class="subtitle">
          异步任务、历史检索与管理员面板一应俱全。
        </p>
      </div>

      <div class="hero-actions" v-if="isAuthenticated">
        <p>欢迎：{{ auth.state.user.username }}（{{ auth.state.user.role }}）</p>
        <button type="button" @click="handleLogout">退出登录</button>
      </div>
      <div class="hero-actions" v-else>
        <span>请先登录以提交任务</span>
      </div>
    </header>

    <main>
      <AuthPanel v-if="!isAuthenticated" />
      <section v-else>
        <nav class="tabs">
          <button :class="{ active: activeTab === 'generator' }" @click="activeTab = 'generator'">文档生成</button>
          <button :class="{ active: activeTab === 'history' }" @click="activeTab = 'history'">历史记录</button>
          <button
            v-if="isAdmin"
            :class="{ active: activeTab === 'admin' }"
            @click="activeTab = 'admin'"
          >管理员</button>
        </nav>

        <component :is="currentComponent" />
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import DocGenerator from './components/DocGenerator.vue';
import AuthPanel from './components/AuthPanel.vue';
import HistoryPanel from './components/HistoryPanel.vue';
import AdminPanel from './components/AdminPanel.vue';
import { useAuthStore } from './stores/auth';

const auth = useAuthStore();
const isAuthenticated = computed(() => auth.isAuthenticated.value);
const isAdmin = computed(() => auth.isAdmin.value);
const activeTab = ref('generator');

const currentComponent = computed(() => {
  if (activeTab.value === 'history') return HistoryPanel;
  if (activeTab.value === 'admin') return AdminPanel;
  return DocGenerator;
});

watch(isAuthenticated, (loggedIn) => {
  if (!loggedIn) {
    activeTab.value = 'generator';
  }
});

const handleLogout = () => {
  auth.logout();
};
</script>

<style scoped>
.page {
  padding: 32px 24px 64px;
  max-width: 1200px;
  margin: 0 auto;
}

.hero {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
  padding: 24px;
  border-radius: 20px;
  background: radial-gradient(circle at 0% 0%, #dbeafe, #eff6ff);
  box-shadow: 0 6px 24px rgba(15, 23, 42, 0.08);
}

.eyebrow {
  margin: 0;
  font-size: 0.9rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #2563eb;
  font-weight: 600;
}

h1 {
  margin: 8px 0 6px;
  font-size: 1.85rem;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #334155;
  max-width: 640px;
}

.hero-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  font-weight: 600;
  color: #0f172a;
}

.hero-actions button {
  padding: 6px 16px;
  border-radius: 999px;
  border: none;
  background: #1d4ed8;
  color: #fff;
}

main {
  margin-top: 32px;
}

.tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.tabs button {
  border-radius: 999px;
  padding: 8px 20px;
  border: 1px solid #cbd5f5;
  background: transparent;
  cursor: pointer;
}

.tabs button.active {
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  color: #fff;
  border-color: transparent;
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
  }
}
</style>
