<template>
  <section class="card admin-panel">
    <header>
      <div>
        <h2>管理员面板</h2>
        <p>管理用户角色、配额以及公开 API-Key。</p>
      </div>
      <button class="ghost" type="button" @click="initialize" :disabled="isLoading">
        {{ isLoading ? '加载中...' : '重新加载' }}
      </button>
    </header>

    <h3>用户管理</h3>
    <table>
      <thead>
        <tr>
          <th>用户名</th>
          <th>邮箱</th>
          <th>角色</th>
          <th>配额(15min)</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.username }}</td>
          <td>
            <input v-model="user.email" />
          </td>
          <td>
            <select v-model="user.role">
              <option value="STANDARD">普通用户</option>
              <option value="PREMIUM">高级用户</option>
              <option value="ADMIN">管理员</option>
            </select>
          </td>
          <td>
            <input type="number" v-model.number="user.apiQuota" />
          </td>
          <td>
            <button type="button" @click="updateUserConfig(user)">保存</button>
          </td>
        </tr>
      </tbody>
    </table>

    <h3>API Key 配置</h3>
    <div class="api-form">
      <input v-model="apiForm.firstName" placeholder="名" />
      <input v-model="apiForm.lastName" placeholder="姓" />
      <input v-model="apiForm.email" placeholder="邮箱" />
      <input v-model="apiForm.purpose" placeholder="用途" />
      <input v-model="apiForm.rawKey" placeholder="原始 API Key" />
      <button type="button" @click="createKey">创建</button>
      <p v-if="lastCreatedKey" class="hint">已创建：{{ lastCreatedKey }}</p>
    </div>

    <table>
      <thead>
        <tr>
          <th>联系人</th>
          <th>邮箱</th>
          <th>用途</th>
          <th>哈希</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="key in apiKeys" :key="key.id">
          <td>{{ key.firstName }} {{ key.lastName }}</td>
          <td>{{ key.email }}</td>
          <td>{{ key.purpose || '-' }}</td>
          <td class="mono">{{ key.hashedKey }}</td>
          <td>
            <button class="danger" type="button" @click="deleteKey(key.id)">删除</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { createApiKey, deleteApiKey, fetchUsers, listApiKeys, updateUser } from '../api/client';

const users = ref([]);
const apiKeys = ref([]);
const isLoading = ref(false);
const lastCreatedKey = ref('');

const apiForm = reactive({
  firstName: '',
  lastName: '',
  email: '',
  purpose: '',
  rawKey: ''
});

const initialize = async () => {
  isLoading.value = true;
  try {
    const [userResp, keyResp] = await Promise.all([fetchUsers(), listApiKeys()]);
    users.value = (userResp.data?.data ?? []).map((user) => ({ ...user }));
    apiKeys.value = keyResp.data?.data ?? [];
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const updateUserConfig = async (user) => {
  try {
    await updateUser(user.id, { email: user.email, role: user.role, apiQuota: user.apiQuota });
  } catch (error) {
    alert(error.message);
  }
};

const createKey = async () => {
  if (!apiForm.rawKey) {
    alert('请填写原始 Key');
    return;
  }
  try {
    const { data } = await createApiKey({ ...apiForm });
    lastCreatedKey.value = data?.data?.rawKey ?? '';
    apiForm.firstName = '';
    apiForm.lastName = '';
    apiForm.email = '';
    apiForm.purpose = '';
    apiForm.rawKey = '';
    initialize();
  } catch (error) {
    alert(error.message);
  }
};

const deleteKey = async (id) => {
  if (!confirm('确定删除该 API Key？')) {
    return;
  }
  try {
    await deleteApiKey(id);
    initialize();
  } catch (error) {
    alert(error.message);
  }
};

onMounted(initialize);
</script>

<style scoped>
.admin-panel h3 {
  margin-top: 24px;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 12px;
}

th, td {
  padding: 10px;
  border-bottom: 1px solid #e2e8f0;
}

input, select {
  width: 100%;
  border: 1px solid #cbd5f5;
  border-radius: 8px;
  padding: 6px 10px;
}

button {
  border-radius: 999px;
  padding: 6px 16px;
}

.danger {
  background: #fee2e2;
  color: #b91c1c;
}

.api-form {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-top: 16px;
  align-items: center;
}

.api-form button {
  justify-self: flex-start;
}

.mono {
  font-family: 'SFMono-Regular', Consolas, monospace;
  font-size: 0.85rem;
}

.hint {
  grid-column: 1 / -1;
  color: #16a34a;
}
</style>
