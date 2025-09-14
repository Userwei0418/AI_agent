<template>
  <div id="app-container">
    <div id="userRegisterPage">
      <h2 class="title">AI应用生成 - 用户注册</h2>
      <div class="desc">代码改变世界</div>
      <a-form
        :model="formState"
        name="basic"
        label-align="left"
        autocomplete="off"
        @finish="handleSubmit"
      >
        <a-form-item
          name="userAccount"
          :rules="[
            { required: true, message: '请输入账号' },
            { max: 20, message: '账号长度不能超过20' },
          ]"
        >
          <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
        </a-form-item>

        <a-form-item
          name="userPassword"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 8, max: 20, message: '密码长度必须在8到20之间' },
          ]"
        >
          <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
        </a-form-item>

        <a-form-item
          name="checkPassword"
          :rules="[
            { required: true, message: '请输入确认密码' },
            { min: 8, max: 20, message: '重复密码长度必须在8到20之间' },
          ]"
        >
          <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
        </a-form-item>

        <a-form-item name="inviteCode">
          <a-input
            v-model:value="formState.inviteCode"
            placeholder="请输入邀请码（可选）"
            :disabled="Boolean(formState.inviteCode)"
          />
        </a-form-item>

        <div class="tips">
          已有账号？
          <RouterLink to="/user/login">去登录</RouterLink>
        </div>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { userRegister } from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'


const formState = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
  inviteCode: '',
})

const router = useRouter()
const loginUserStore = useLoginUserStore()

const handleSubmit = async (values: any) => {
  if (values.userPassword !== values.checkPassword) {
    message.error('两次输入的密码不一致')
    return
  }

  try {
    const res = await userRegister(values)
    if (res.data.code === 0 && res.data.data) {
      await loginUserStore.fetchLoginUser()
      message.success('注册成功')
      router.push({ path: '/user/login', replace: true })
    } else {
      message.error(`注册失败，${res.data.description || '请稍后再试'}`)
    }
  } catch (error) {
    message.error('网络请求失败，请检查网络连接')
  }
}

import { onMounted } from 'vue'

onMounted(() => {
  const urlParams = new URLSearchParams(window.location.search);
  const inviteCode = urlParams.get('inviteCode');
  if (inviteCode) {
    formState.inviteCode = inviteCode;
  }
})

</script>

<style>
#app-container {
  height: 100vh;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  display: flex;
  align-items: flex-start;
  padding-top: 200px;
  justify-content: center;
}

#userRegisterPage {
  background: rgba(255, 255, 255, 0.75);
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  width: 380px;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
