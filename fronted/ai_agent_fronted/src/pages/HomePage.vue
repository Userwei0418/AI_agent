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
import { userLoginBySession } from '@/api/userController.ts'

const loginResult = ref<string>('');

const handleLogin = async () => {
  try {
    const result = await userLoginBySession();
    loginResult.value = result.data || '成功';
    console.log('结果:', result);
  } catch (error) {
    loginResult.value = '失败' ;
    console.error('错误:', error);
  }
};
</script>

<style scoped>
</style>
