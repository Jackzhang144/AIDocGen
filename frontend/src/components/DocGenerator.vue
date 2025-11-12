<template>
  <section class="card">
    <form class="form" @submit.prevent="handleSubmit">
      <div class="field two-columns">
        <label>
          <span>代码片段 *</span>
          <textarea
            v-model="form.code"
            rows="8"
            placeholder="在此粘贴需要生成文档的代码..."
          ></textarea>
        </label>

        <label>
          <span>上下文（可选）</span>
          <textarea
            v-model="form.context"
            rows="8"
            placeholder="当没有选区信息时，可提供更大的上下文。"
          ></textarea>
        </label>
      </div>

      <div class="field">
        <label>
          <span>语言 *</span>
          <select v-model="form.languageId">
            <option v-for="lang in languages" :key="lang.value" :value="lang.value">
              {{ lang.label }}
            </option>
          </select>
        </label>
      </div>

      <div class="field advanced">
        <label>
          <span>文档格式</span>
          <select v-model="form.docStyle">
            <option v-for="style in docStyles" :key="style.value" :value="style.value">
              {{ style.label }}
            </option>
          </select>
        </label>

        <label>
          <span>最大行宽</span>
          <input v-model.number="form.width" type="number" min="20" max="160" />
        </label>

        <label class="checkbox">
          <input v-model="form.commented" type="checkbox" />
          <span>自动加注释包裹</span>
        </label>

        <label class="checkbox">
          <input v-model="form.isSelection" type="checkbox" />
          <span>选区模式</span>
        </label>
      </div>

      <div class="actions">
        <button type="submit" :disabled="isSubmitting">
          {{ isSubmitting ? '提交中...' : '提交任务' }}
        </button>
        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
      </div>
    </form>
  </section>

  <section v-if="jobMeta.id" class="card status">
    <header>
      <div>
        <p>任务 ID</p>
        <code>{{ jobMeta.id }}</code>
      </div>
      <div class="state" :data-state="jobMeta.state">
        {{ stateCopy[jobMeta.state] ?? jobMeta.state }}
      </div>
    </header>
    <p v-if="jobMeta.reason && jobMeta.state === 'FAILED'" class="error">
      {{ jobMeta.reason }}
    </p>
    <p v-if="jobMeta.state !== 'FAILED' && !result" class="hint">
      系统每 {{ pollIntervalMs / 1000 }} 秒轮询一次。您可继续编辑下一段代码。
    </p>
  </section>

  <section v-if="result" class="card result">
    <header>
      <h2>生成结果</h2>
      <div class="feedback">
        <span>满意度：</span>
        <button
          type="button"
          class="ghost"
          :disabled="feedbackState === 1"
          @click="handleFeedback(1)"
        >
          👍
        </button>
        <button
          type="button"
          class="ghost"
          :disabled="feedbackState === -1"
          @click="handleFeedback(-1)"
        >
          👎
        </button>
      </div>
    </header>

    <article>
      <h3>Documentation</h3>
      <pre>{{ result.documentation }}</pre>

      <h3 v-if="result.preview">Preview</h3>
      <pre v-if="result.preview">{{ result.preview }}</pre>
    </article>
  </section>
</template>

<script setup>
import { onUnmounted, reactive, ref } from 'vue';
import { fetchJobStatus, submitDocJob, submitFeedback } from '../api/client';

const pollIntervalMs = 2000;

const form = reactive({
  code: '',
  context: '',
  languageId: 'python',
  docStyle: 'auto',
  commented: true,
  isSelection: true,
  width: 80
});

const languages = [
  { label: 'Python', value: 'python' },
  { label: 'Java', value: 'java' },
  { label: 'TypeScript', value: 'ts' },
  { label: 'Go', value: 'go' },
  { label: 'C#', value: 'csharp' },
  { label: 'C++', value: 'cpp' }
];

const docStyles = [
  { label: '自动检测', value: 'auto' },
  { label: 'Javadoc', value: 'javadoc' },
  { label: 'reStructuredText', value: 'rst' },
  { label: 'Google Style', value: 'google' }
];

const isSubmitting = ref(false);
const errorMessage = ref('');
const jobMeta = reactive({
  id: '',
  state: '',
  reason: ''
});
const result = ref(null);
const feedbackState = ref(0);
let pollTimer = null;

const stateCopy = {
  PENDING: '排队中',
  IN_PROGRESS: '生成中',
  SUCCEEDED: '已完成',
  FAILED: '失败'
};

const resetJobState = () => {
  jobMeta.id = '';
  jobMeta.state = '';
  jobMeta.reason = '';
  result.value = null;
  feedbackState.value = 0;
};

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
};

const startPolling = (id) => {
  stopPolling();
  pollTimer = setInterval(async () => {
    try {
      const { data } = await fetchJobStatus(id);
      const payload = data?.data;
      if (!payload) {
        return;
      }
      jobMeta.id = payload.id;
      jobMeta.state = payload.state;
      jobMeta.reason = payload.reason;

      if (payload.state === 'SUCCEEDED') {
        result.value = payload.data;
        stopPolling();
      } else if (payload.state === 'FAILED') {
        stopPolling();
      }
    } catch (error) {
      errorMessage.value = `轮询失败：${error.message}`;
      stopPolling();
    }
  }, pollIntervalMs);
};

const handleSubmit = async () => {
  if (!form.code && !form.context) {
    errorMessage.value = '至少填写代码或上下文其一';
    return;
  }
  isSubmitting.value = true;
  errorMessage.value = '';
  result.value = null;
  feedbackState.value = 0;

  try {
    const payload = {
      code: form.code || undefined,
      context: form.context || undefined,
      languageId: form.languageId,
      commented: form.commented,
      docStyle: form.docStyle === 'auto' ? null : form.docStyle,
      width: form.width,
      isSelection: form.isSelection,
      source: 'web'
    };

    const { data } = await submitDocJob(payload);
    const id = data?.data?.id;
    if (!id) {
      throw new Error('未能获取任务 ID');
    }

    jobMeta.id = id;
    jobMeta.state = 'PENDING';
    jobMeta.reason = '';
    startPolling(id);
  } catch (error) {
    errorMessage.value = error.message;
    resetJobState();
  } finally {
    isSubmitting.value = false;
  }
};

const handleFeedback = async (score) => {
  if (!result.value?.feedbackId) {
    errorMessage.value = '当前结果没有反馈标识';
    return;
  }

  try {
    await submitFeedback({
      id: result.value.feedbackId,
      feedback: score
    });
    feedbackState.value = score;
  } catch (error) {
    errorMessage.value = error.message;
  }
};

onUnmounted(() => {
  stopPolling();
});
</script>

<style scoped>
.card {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 30px 60px rgba(15, 23, 42, 0.08);
  margin-bottom: 24px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.two-columns {
  flex-direction: row;
}

.two-columns label {
  flex: 1;
}

label span {
  display: inline-block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #0f172a;
}

textarea,
select,
input[type='number'] {
  width: 100%;
  border: 1px solid #cbd5f5;
  border-radius: 12px;
  padding: 12px 16px;
  background: #f8fafc;
  font: inherit;
  transition: border-color 0.2s ease;
}

textarea:focus,
select:focus,
input[type='number']:focus {
  outline: none;
  border-color: #2563eb;
  background: #fff;
}

.advanced {
  flex-direction: row;
  flex-wrap: wrap;
  gap: 16px;
}

.advanced label {
  flex: 1 1 200px;
}

.checkbox {
  flex-direction: row;
  align-items: center;
  gap: 8px;
}

.checkbox input {
  width: auto;
}

.actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.actions button {
  padding: 12px 28px;
  border-radius: 999px;
  background: linear-gradient(135deg, #2563eb, #4f46e5);
  color: #fff;
  font-size: 1rem;
}

.error {
  color: #dc2626;
  margin: 0;
}

.hint {
  color: #475569;
}

.status header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.state {
  padding: 6px 16px;
  border-radius: 999px;
  font-weight: 600;
  color: #1d1f2b;
}

.state[data-state='PENDING'],
.state[data-state='IN_PROGRESS'] {
  background: #fee2e2;
}

.state[data-state='SUCCEEDED'] {
  background: #dcfce7;
}

.state[data-state='FAILED'] {
  background: #fee2e2;
}

.result article pre {
  padding: 16px;
  border-radius: 16px;
  background: #0f172a;
  color: #f8fafc;
  overflow-x: auto;
}

.feedback {
  display: flex;
  align-items: center;
  gap: 8px;
}

.feedback .ghost {
  background: transparent;
  border: 1px solid #cbd5f5;
  border-radius: 999px;
  padding: 6px 16px;
}

@media (max-width: 768px) {
  .two-columns {
    flex-direction: column;
  }

  .advanced {
    flex-direction: column;
  }
}
</style>
