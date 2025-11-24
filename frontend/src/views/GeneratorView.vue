<template>
  <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <div class="lg:col-span-1">
      <GeneratorForm />
    </div>
    <div class="lg:col-span-1">
      <JobStatusCard />
      <DocResultPanel />
    </div>
    <div class="lg:col-span-1">
      <HistoryList />
    </div>
  </div>
</template>

<script setup>
import GeneratorForm from '../components/GeneratorForm.vue';
import JobStatusCard from '../components/JobStatusCard.vue';
import DocResultPanel from '../components/DocResultPanel.vue';
import HistoryList from '../components/HistoryList.vue';
</script>
