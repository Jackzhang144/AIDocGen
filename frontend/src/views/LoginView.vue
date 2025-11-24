<template>
  <div class="flex justify-center items-center h-screen">
    <AuthForm :is-login="true" />
  </div>
</template>

<script setup>
import AuthForm from '../components/AuthForm.vue';
</script>
