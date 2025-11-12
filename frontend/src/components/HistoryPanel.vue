<template>
  <section class="card history-panel">
    <header>
      <div>
        <h2>生成历史</h2>
        <p>查看最近的文档生成记录，可按关键字与语言筛选。</p>
      </div>
      <button class="ghost" type="button" @click="fetchRecords" :disabled="isLoading">
        {{ isLoading ? '刷新中...' : '刷新' }}
      </button>
    </header>

    <div class="filters">
      <input v-model="filters.keyword" placeholder="关键字" />
      <input v-model="filters.language" placeholder="语言" />
      <input v-model="filters.source" placeholder="来源" />
      <button type="button" @click="applyFilters">应用</button>
    </div>

    <table>
      <thead>
        <tr>
          <th>时间</th>
          <th>语言</th>
          <th>模型</th>
          <th>耗时(ms)</th>
          <th>预览</th>
          <th>反馈</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in records" :key="item.id">
          <td>{{ formatTime(item.timestamp) }}</td>
          <td>{{ item.language || '-' }}</td>
          <td>{{ item.modelProvider || '-' }}</td>
          <td>{{ item.timeToGenerate ?? '-' }}</td>
          <td>
            <details>
              <summary>查看</summary>
              <pre>{{ item.outputPreview || '无输出' }}</pre>
            </details>
          </td>
          <td>{{ formatFeedback(item.feedback) }}</td>
        </tr>
        <tr v-if="!records.length && !isLoading">
          <td colspan="6" class="empty">暂无记录</td>
        </tr>
      </tbody>
    </table>

    <footer class="pagination">
      <button type="button" :disabled="page === 1" @click="changePage(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
      <button type="button" :disabled="page === totalPages" @click="changePage(page + 1)">下一页</button>
    </footer>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue';
import { fetchHistory } from '../api/client';

const records = ref([]);
const page = ref(1);
const size = ref(5);
const total = ref(0);
const isLoading = ref(false);
const filters = reactive({ keyword: '', language: '', source: '' });

const totalPages = computed(() => (total.value === 0 ? 1 : Math.ceil(total.value / size.value)));

const fetchRecords = async () => {
  isLoading.value = true;
  try {
    const { data } = await fetchHistory({
      page: page.value,
      size: size.value,
      keyword: filters.keyword || undefined,
      language: filters.language || undefined,
      source: filters.source || undefined
    });
    const payload = data?.data;
    records.value = payload?.records ?? [];
    total.value = payload?.total ?? 0;
  } catch (error) {
    console.error(error);
  } finally {
    isLoading.value = false;
  }
};

const changePage = (nextPage) => {
  page.value = Math.min(Math.max(nextPage, 1), totalPages.value);
  fetchRecords();
};

const applyFilters = () => {
  page.value = 1;
  fetchRecords();
};

const formatTime = (value) => {
  if (!value) return '-';
  return new Date(value).toLocaleString();
};

const formatFeedback = (score) => {
  if (score === 1) return '👍';
  if (score === -1) return '👎';
  if (score === 0 || score === null || score === undefined) return '-';
  return score;
};

onMounted(fetchRecords);
</script>

<style scoped>
.history-panel table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 16px;
}

th, td {
  padding: 12px;
  border-bottom: 1px solid #e2e8f0;
  text-align: left;
}

pre {
  white-space: pre-wrap;
  max-height: 180px;
  overflow: auto;
}

.filters {
  display: flex;
  gap: 12px;
  margin-top: 12px;
}

.filters input {
  flex: 1;
  border: 1px solid #cbd5f5;
  border-radius: 10px;
  padding: 8px 12px;
}

.filters button {
  border-radius: 999px;
  padding: 8px 16px;
  background: #1d4ed8;
  color: #fff;
}

.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16px;
}

.pagination button {
  border-radius: 999px;
  padding: 8px 16px;
}

.empty {
  text-align: center;
  color: #94a3b8;
}
</style>
