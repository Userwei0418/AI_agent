<template>
  <div style="text-align: center; margin-top: 50px;">
    <h1>403 Forbidden</h1>
    <p>您没有权限访问该页面。</p>
    <a-button type="primary" @click="goBack">返回上一页</a-button>
  </div>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}
</script>
