<template>
  <a-layout-content class="user-profile-page">
    <!-- 基础信息卡片 -->
    <a-card title="基础信息" class="profile-card">
      <a-form @submit.prevent="updateBaseInfo" :model="baseInfo" layout="vertical">
        <a-form-item label="头像">
          <div @click="handleAvatarClick" style="cursor: pointer">
            <a-avatar :src="baseInfo.userAvatar" size="large" />
            <input
              type="file"
              ref="avatarInput"
              @change="handleAvatarChange"
              style="display: none"
              accept="image/*"
            />
          </div>
        </a-form-item>
        <a-form-item label="姓名">
          <a-input v-model:value="baseInfo.userName" placeholder="姓名" />
        </a-form-item>
        <a-form-item label="简介">
          <a-input v-model:value="baseInfo.userProfile" placeholder="简介" />
        </a-form-item>
        <a-form-item label="邀请码">
          <a-input
            :value="loginUserStore.loginUser.shareCode"
            placeholder="邀请码"
            disabled
            readonly
          />
        </a-form-item>
        <a-form-item label="邀请激励">
          <div>
            你已经邀请
            {{
              loginUserStore.loginUser.inviteUserNumber
            }}
            人，加油，让更多人发现我们的AI智能体平台！
          </div>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" @click="handleShare" style="margin-right: 20px">生成分享链接</a-button>
          <a-button type="primary" html-type="submit">更新基础信息</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 联系信息卡片 -->
    <a-card title="联系信息" class="profile-card">
      <a-form @submit.prevent="updateContactInfo" :model="contactInfo" layout="vertical">
        <a-form-item label="邮箱">
          <a-input v-model:value="contactInfo.userEmail" placeholder="邮箱" />
        </a-form-item>
        <a-form-item label="电话">
          <a-input v-model:value="contactInfo.userPhone" placeholder="电话" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">更新联系信息</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 密码修改卡片（保持原有逻辑） -->
    <a-card title="安全设置" class="profile-card">
      <a-form @submit.prevent="updateUserPassword" :model="passwordUpdate" layout="vertical">
        <a-form-item label="旧密码">
          <a-input-password v-model:value="passwordUpdate.oldPassword" placeholder="旧密码" />
        </a-form-item>
        <a-form-item label="新密码">
          <a-input-password v-model:value="passwordUpdate.newPassword" placeholder="新密码" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">更新密码</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </a-layout-content>
</template>

<script setup lang="ts">
import { reactive, onMounted, ref } from 'vue'
import {
  updatePassword,
  updateUser,
  uploadAvatar,
} from '@/api/userController.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'

// 基础信息表单数据
const baseInfo = reactive({
  id: 0,
  userAvatar: '',
  userName: '',
  userProfile: '',
})

// 联系信息表单数据
const contactInfo = reactive({
  id: 0,
  userEmail: '',
  userPhone: '',
})

// 密码修改表单（保持原有结构）
const passwordUpdate = reactive({
  oldPassword: '',
  newPassword: '',
})

const loginUserStore = useLoginUserStore()
const router = useRouter()

// 初始化用户数据
const fetchUserInfo = async () => {
  try {
    await loginUserStore.fetchLoginUser()
    const user = loginUserStore.loginUser
    if (user) {
      // 基础信息
      baseInfo.id = user.id || 0
      baseInfo.userAvatar = user.userAvatar || ''
      baseInfo.userName = user.userName || ''
      baseInfo.userProfile = user.userProfile || ''

      // 联系信息
      contactInfo.id = user.id || 0
      contactInfo.userEmail = user.userEmail || ''
      contactInfo.userPhone = user.userPhone || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const avatarInput = ref<HTMLInputElement | null>(null)
const handleAvatarClick = () => {
  if (avatarInput.value) {
    avatarInput.value.click()
  }
}
const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  if (target.files && target.files[0]) {
    const file = target.files[0]
    try {
      const response = await uploadAvatar({}, file)
      if (response.data.code === 0) {
        baseInfo.userAvatar = response.data.data.userAvatar
        await loginUserStore.fetchLoginUser()
        router.go(0)
      } else {
        message.error(`上传失败：${response.data.description || '未知错误'}`)
      }
    } catch (error) {
      console.error('上传失败:', error)
      message.error('网络请求失败')
    }
  }
}

// 更新基础信息
const updateBaseInfo = async () => {
  try {
    const response = await updateUser({
      id: baseInfo.id,
      userAvatar: baseInfo.userAvatar,
      userName: baseInfo.userName,
      userProfile: baseInfo.userProfile,
    })

    if (response.data.code === 0) {
      message.success('基础信息更新成功')
      await loginUserStore.fetchLoginUser()
      router.go(0)
    } else {
      message.error(`更新失败：${response.data.description || '未知错误'}`)
    }
  } catch (error) {
    console.error('更新失败:', error)
    message.error('网络请求失败')
  }
}

// 更新联系信息
const updateContactInfo = async () => {
  try {
    const response = await updateUser({
      id: contactInfo.id,
      userEmail: contactInfo.userEmail,
      userPhone: contactInfo.userPhone,
    })

    if (response.data.code === 0) {
      message.success('联系信息更新成功')
      await loginUserStore.fetchLoginUser()
    } else {
      message.error(`更新失败：${response.data.description || '未知错误'}`)
    }
  } catch (error) {
    console.error('更新失败:', error)
    message.error('网络请求失败')
  }
}

// 保持原有密码修改逻辑
const updateUserPassword = async () => {
  try {
    const response = await updatePassword(passwordUpdate)
    if (response.data.code === 0) {
      message.success('密码更新成功')
      router.push({ path: '/user/login', replace: true })
    } else {
      message.error(`更新失败：${response.data.description || '未知错误'}`)
    }
  } catch (error) {
    console.error('更新失败:', error)
    message.error('网络请求失败')
  }
}

onMounted(() => {
  fetchUserInfo()
})

const getBaseUrl = (): string => {
  if (import.meta.env.MODE === 'production') {
    return import.meta.env.VITE_APP_BASEURL
  } else {
    return `http://${import.meta.env.VITE_APP_HOST}:${import.meta.env.VITE_APP_PORT}`
  }
}
const generateShareLink = (): string => {
  const baseUrl = getBaseUrl()
  const shareCode = loginUserStore.loginUser.shareCode
  return `${baseUrl}/user/register?inviteCode=${shareCode}`
}
const shareLink = ref<string>('')

const handleShare = () => {
  const link = generateShareLink()
  shareLink.value = link
  navigator.clipboard
    .writeText(link)
    .then(() => {
      message.success('链接已复制到剪贴板')
    })
    .catch((err) => {
      message.error('复制链接失败')
    })
}
</script>

<style scoped>
.user-profile-page {
  padding: 24px;
  background: #f0f2f5;
  min-height: 100vh;
}

.card-container {
  display: flex;
  flex-direction: column;
  gap: 48px;
  max-width: 600px;
  margin: 0 auto;
}

.profile-card {
  margin: 30px auto;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-width: 800px;
  padding: 24px;
}
</style>
