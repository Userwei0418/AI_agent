<template>
  <div>
    <h1>首页</h1>
    <a-button type="primary" @click="handleLogin">测试</a-button>
    <p v-if="loginResult">{{ loginResult }}</p>
  </div>
</template>

<script setup lang="ts">
// @ts-ignore
/* eslint-disable */
import { ref } from 'vue';
import { userLoginBySession } from '@/api/healthController.ts'

const loginResult = ref<string>('');

const handleLogin = async () => {
  try {
    const result = await userLoginBySession();
    loginResult.value = result.data || '登录成功';
    console.log('登录结果:', result);
  } catch (error) {
    loginResult.value = '登录失败';
    console.error('登录错误:', error);
  }
};
</script>

<style scoped>
</style>
