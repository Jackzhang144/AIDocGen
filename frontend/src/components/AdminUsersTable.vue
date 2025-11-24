<template>
  <div class="bg-white rounded-lg shadow-md">
    <div class="flex justify-between items-center p-4">
      <h3 class="text-lg font-bold">{{ t('adminUserTitle') }}</h3>
      <button @click="fetchUsers" class="px-3 py-1 text-sm rounded-md border border-gray-300">{{ t('refresh') }}</button>
    </div>
    <table class="min-w-full divide-y divide-gray-200">
      <thead class="bg-gray-50">
        <tr>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('emailLabel') }}</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('username') }}</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('role') }}</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('quota') }}</th>
          <th class="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('createdAt') }}</th>
          <th class="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">{{ t('actions') }}</th>
        </tr>
      </thead>
      <tbody class="bg-white divide-y divide-gray-200">
        <tr v-for="user in users" :key="user.id">
          <td class="px-6 py-4 whitespace-nowrap">{{ user.email }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ user.username }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ user.role }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ user.apiQuota ?? 'N/A' }}</td>
          <td class="px-6 py-4 whitespace-nowrap">{{ new Date(user.createdAt).toLocaleDateString() }}</td>
          <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
            <button
              @click="openEdit(user)"
              :disabled="user.role === 'ADMIN'"
              class="text-indigo-600 hover:text-indigo-900 disabled:text-gray-400 disabled:cursor-not-allowed">
              {{ t('edit') }}
            </button>
          </td>
        </tr>
      </tbody>
    </table>
    <div v-if="editingUser" class="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
      <div class="bg-white rounded-lg shadow-lg w-full max-w-md p-6 space-y-4">
        <h4 class="text-lg font-bold">{{ t('edit') }}</h4>
        <div class="space-y-3">
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('emailLabel') }}</label>
            <input v-model="form.email" type="email" class="mt-1 w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('role') }}</label>
            <select v-model="form.role" class="mt-1 w-full border rounded px-3 py-2">
              <option value="STANDARD">STANDARD</option>
              <option value="PREMIUM">PREMIUM</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700">{{ t('quota') }}</label>
            <input v-model.number="form.apiQuota" :disabled="isUnlimited" type="number" class="mt-1 w-full border rounded px-3 py-2" />
            <p class="text-xs text-gray-500">STANDARD 可设置具体配额；PREMIUM/ADMIN 自动为无限额（-1）。</p>
          </div>
        </div>
        <div class="flex justify-end gap-2">
          <button @click="closeEdit" class="px-4 py-2 border rounded">{{ t('cancel') }}</button>
          <button @click="saveEdit" :disabled="saving" class="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 disabled:opacity-60">
            {{ saving ? '...' : t('save') }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import apiClient from '../api/client';
import { useI18n } from '../i18n';

const users = ref([]);
const { t } = useI18n();
const editingUser = ref(null);
const saving = ref(false);
const form = reactive({
  id: null,
  email: '',
  role: 'USER',
  apiQuota: null
});
const isUnlimited = computed(() => form.role === 'PREMIUM' || form.role === 'ADMIN');

watch(() => form.role, (role) => {
  if (role === 'PREMIUM' || role === 'ADMIN') {
    form.apiQuota = -1;
  } else if (form.apiQuota === null || form.apiQuota < 0) {
    form.apiQuota = 50;
  }
});

async function fetchUsers() {
  try {
    const data = await apiClient.fetchUsers();
    users.value = data;
  } catch (error) {
    console.error('Failed to fetch users:', error);
  }
}

function openEdit(user) {
  editingUser.value = user;
  form.id = user.id;
  form.email = user.email;
  form.role = user.role;
  form.apiQuota = user.apiQuota ?? 50;
}

function closeEdit() {
  editingUser.value = null;
}

async function saveEdit() {
  if (!editingUser.value) return;
  saving.value = true;
  try {
    await apiClient.updateUser(form.id, {
      email: form.email,
      role: form.role,
      apiQuota: form.apiQuota
    });
    await fetchUsers();
    closeEdit();
  } catch (error) {
    console.error('Failed to update user:', error);
  } finally {
    saving.value = false;
  }
}

onMounted(fetchUsers);
</script>
