<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline">
      <a-form-item label="搜索">
        <a-input v-model:value="searchParams.keyword" placeholder="请输入搜索关键词" allow-clear />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" @click="doSearch">搜索</a-button>
      </a-form-item>

      <a-form-item>
        <a-button type="primary" @click="openSendNotificationModal(null)">发送全体通知</a-button>
      </a-form-item>
    </a-form>

    <div style="margin-bottom: 16px" />

    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="getSafeImageUrl(record.userAvatar)" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-tag :color="getUserRoleColor(record.roles)">{{ getUserRoleText(record.roles) }}</a-tag>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="dashed" @click="openSendNotificationModal(record.id)"
            >发送通知
            </a-button>
            <a-button type="dashed" @click="openSetRoleModal(record.id)">设置角色</a-button>
            <a-button type="dashed" @click="confirmResetPassword(record.id)">重置密码</a-button>
            <a-switch
              :checked="record.userStatus === 'ACTIVE'"
              @change="toggleUserStatus(record.id, record.userStatus)"
              :title="record.userStatus === 'ACTIVE' ? '禁用用户' : '启用用户'"
            />
            <a-button danger @click="confirmDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 设置角色的模态框 -->
    <a-modal
      v-model:visible="setRoleModalVisible"
      title="设置角色"
      @ok="handleSetRoleOk"
      @cancel="handleSetRoleCancel"
      :centered="true"
      :wrapClassName="customModalClass"
    >
      <a-checkbox-group v-model:value="selectedRoles">
        <a-checkbox :value="role.id" v-for="role in roles" :key="role.id">
          {{ role.text }}
        </a-checkbox>
      </a-checkbox-group>
    </a-modal>

    <!-- 发送通知模态框 -->
    <a-modal
      v-model:visible="notificationModalVisible"
      :title="notificationSelectedUserId ? '发送用户通知' : '发送全体通知'"
      @ok="handleSendNotification"
      @cancel="closeNotificationModal"
      :confirm-loading="sending"
    >
      <a-form layout="vertical">
        <a-form-item label="通知标题" required :rules="[{ required: true, message: '请输入标题' }]">
          <a-input v-model:value="notificationForm.title" />
        </a-form-item>
        <a-form-item label="通知内容" required :rules="[{ required: true, message: '请输入内容' }]">
          <a-textarea
            v-model:value="notificationForm.content"
            :rows="4"
            :maxlength="500"
            show-count
          />
        </a-form-item>
      </a-form>
    </a-modal>

  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  deleteUser,
  getAllUsers,
  searchUsers,
  resetUserPassword,
  updateUserStatus,
} from '@/api/userController.ts'

import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import { createUserRole, deleteUserRole } from '@/api/userRoleController.ts'
import { sendNotification } from '@/api/messageController.ts'

const columns = [
  {
    title: 'ID',
    dataIndex: 'id',
    align: 'center',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    align: 'center',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
    align: 'center',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
    align: 'center',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
    align: 'center',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
    align: 'center',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    align: 'center',
  },
  {
    title: '操作',
    key: 'action',
    align: 'center',
  },
]

const dataList = ref<API.UserVO[]>([])
const total = ref(0)

const searchParams = reactive<API.searchUsersParams>({
  page: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
  keyword: '',
})

const handleApiResponse = (res: any, successMessage: string, errorMessage: string) => {
  if (res.data.code === 0 && res.data.data) {
    message.success(successMessage)
    return true
  } else {
    message.error(`${errorMessage}${res.data.message}`)
    return false
  }
}

// 获取用户数据
const fetchUsers = async (params?: API.searchUsersParams) => {
  try {
    const res = await searchUsers({
      ...searchParams,
      ...(params || {}),
    })
    if (handleApiResponse(res, '获取成功', '获取数据失败，')) {
      dataList.value = res.data.data.records.map((record) => ({
        ...record,
        userStatus: mapUserStatusToFrontend(record.userStatus),
      }))
      total.value = res.data.data.total ?? 0
    }
  } catch (error) {
    message.error('网络错误，请稍后再试')
  }
}

// 页面加载时获取所有用户数据
onMounted(() => {
  fetchUsers()
})

// 分页参数
const pagination = computed(() => ({
  current: searchParams.page,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (total: number) => `共 ${total} 条`,
}))

// 表格变化之后，重新获取数据
const doTableChange = (page: any) => {
  searchParams.page = page.current
  searchParams.pageSize = page.pageSize
  fetchUsers(searchParams.keyword ? searchParams : undefined)
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.page = 1
  fetchUsers(searchParams)
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    message.error('无效的用户ID')
    return
  }
  try {
    const res = await deleteUser({ userId: id })
    if (res.data.code === 0) {
      message.success('删除成功')
      fetchUsers(searchParams.keyword ? searchParams : undefined)
    } else {
      message.error('删除失败，' + res.data.description)
    }
  } catch (error) {
    console.error('删除用户时发生错误:', error)
    message.error('删除用户时发生错误，请稍后再试')
  }
}

// 确认删除
const confirmDelete = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '此操作将永久删除该用户，是否继续？',
    onOk: () => {
      doDelete(id)
    },
    onCancel() {
      // 用户取消操作
    },
  })
}

// 重置密码
const doResetPassword = async (id: number) => {
  if (!id) {
    message.error('无效的用户ID')
    return
  }
  try {
    const res = await resetUserPassword({ userId: id })
    if (res.data.code === 0) {
      message.success('重置密码成功')
    } else {
      message.error('重置密码失败，' + res.data.description)
    }
  } catch (error) {
    console.error('重置密码时发生错误:', error)
    message.error('重置密码时发生错误，请稍后再试')
  }
}

// 确认重置密码
const confirmResetPassword = (id: number) => {
  Modal.confirm({
    title: '确认重置密码',
    content: '此操作将重置该用户的密码，是否继续？',
    onOk: () => {
      doResetPassword(id)
    },
    onCancel() {
      // 用户取消操作
    },
  })
}

// 更新状态
const toggleUserStatus = async (id: number, currentStatus: string) => {
  if (!id) {
    message.error('无效的用户ID')
    return
  }

  const confirmTitle = currentStatus === 'ACTIVE' ? '是否禁用该用户？' : '是否启用该用户？'
  Modal.confirm({
    title: confirmTitle,
    content: '此操作将更改用户的状态。',
    onOk: async () => {
      const newStatus = currentStatus === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
      try {
        const res = await updateUserStatus({ userId: id, status: newStatus })
        if (res.data.code === 0) {
          message.success('更新状态成功')
          const user = dataList.value.find((user) => user.id === id)
          if (user) {
            user.userStatus = newStatus
          }
        } else {
          message.error('更新状态失败，' + res.data.description)
        }
      } catch (error) {
        console.error('更新用户状态时发生错误:', error)
        message.error('更新用户状态时发生错误，请稍后再试')
      }
    },
    onCancel() {
      // 用户取消操作
    },
  })
}

// 设置角色
// todo debug 删除角色会清理所有角色 待修复 另外 这些逻辑应该移到后端 提供方便的接口
const selectedUserId = ref<number | null>(null)
const selectedRoles = ref<number[]>([])
const setRoleModalVisible = ref(false)
const roles = ref([
  { id: 1, text: '普通用户' },
  { id: 2, text: 'VIP用户' },
  { id: 3, text: '管理员' },
])

const openSetRoleModal = (userId: number) => {
  selectedUserId.value = userId
  const user = dataList.value.find((user) => user.id === userId)
  if (user && user.roles) {
    selectedRoles.value = user.roles.map((roleName) => getRoleIdByName(roleName))
  } else {
    selectedRoles.value = []
  }
  setRoleModalVisible.value = true
}

const handleSetRoleOk = async () => {
  if (!selectedUserId.value) {
    message.error('请选择用户')
    return
  }

  const user = dataList.value.find((user) => user.id === selectedUserId.value)
  if (!user) {
    message.error('用户不存在')
    return
  }

  const currentRoles = user.roles ? user.roles.map((roleName) => getRoleIdByName(roleName)) : []

  // 创建新的角色分配
  const newRoles = selectedRoles.value.filter((roleId) => !currentRoles.includes(roleId))
  for (const roleId of newRoles) {
    const res = await createUserRole({ userId: selectedUserId.value, roleId: roleId })
    if (res.data.code !== 0) {
      message.error('设置角色失败，' + res.data.description)
      return
    }
  }

  // 删除旧的角色分配
  const oldRoles = currentRoles.filter((roleId) => !selectedRoles.value.includes(roleId))
  for (const roleId of oldRoles) {
    const res = await deleteUserRole({ userId: selectedUserId.value, roleId: roleId })
    if (res.data.code !== 0) {
      message.error('删除角色失败，' + res.data.description)
      return
    }
  }

  message.success('设置角色成功')
  setRoleModalVisible.value = false
  fetchUsers(searchParams.keyword ? searchParams : undefined)
}

const handleSetRoleCancel = () => {
  setRoleModalVisible.value = false
}

// 辅助函数：根据 roleName 获取 roleId
const getRoleIdByName = (roleName: string): number => {
  switch (roleName) {
    case 'USER':
      return 1
    case 'VIP':
      return 2
    case 'ADMIN':
      return 3
    default:
      return 0
  }
}

// 辅助函数：根据 roleId 获取 roleName
const getRoleNameById = (roleId: number): string => {
  switch (roleId) {
    case 1:
      return 'USER'
    case 2:
      return 'VIP'
    case 3:
      return 'ADMIN'
    default:
      return ''
  }
}

// 辅助函数：将后端状态映射到前端状态
const mapUserStatusToFrontend = (backendStatus: string): string => {
  switch (backendStatus) {
    case '正常':
      return 'ACTIVE'
    case '禁用':
      return 'DISABLED'
    default:
      return 'UNKNOWN'
  }
}

// 辅助函数：获取安全的图片URL
const getSafeImageUrl = (url: string): string => {
  return url.startsWith('http') ? url : ''
}

// 辅助函数：获取用户角色颜色
const getUserRoleColor = (roles: string[]): string => {
  if (roles.includes('ADMIN')) return 'green'
  if (roles.includes('VIP')) return 'yellow'
  return 'blue'
}

// 辅助函数：获取用户角色文本
const getUserRoleText = (roles: string[]): string => {
  if (roles.includes('ADMIN')) return '管理员'
  if (roles.includes('VIP')) return 'VIP用户'
  return '普通用户'
}

const customModalClass = 'custom-modal'


// 通知相关状态
const notificationModalVisible = ref(false)
const sending = ref(false)
const notificationSelectedUserId = ref<number | null>(null)
const notificationForm = reactive({
  title: '',
  content: ''
})

// 打开通知模态框
const openSendNotificationModal = (userId: number | null) => {
  notificationSelectedUserId.value = userId
  notificationModalVisible.value = true
}

// 关闭模态框
const closeNotificationModal = () => {
  notificationModalVisible.value = false
  notificationForm.title = ''
  notificationForm.content = ''
}

// 发送通知处理
const handleSendNotification = async () => {
  if (!notificationForm.title || !notificationForm.content) {
    message.error('请填写完整的通知内容')
    return
  }

  sending.value = true
  try {
    const params = {
      title: notificationForm.title,
      content: notificationForm.content,
      userId: notificationSelectedUserId.value
    }

    const res = await sendNotification(params)
    if (res.data.code === 0) {
      message.success(notificationSelectedUserId.value ? '用户通知发送成功' : '全体通知发送成功')
      closeNotificationModal()
    } else {
      message.error(`发送失败：${res.data.message}`)
    }
  } catch (error) {
    message.error('网络请求失败，请检查连接')
  } finally {
    sending.value = false
  }
}

</script>

<style scoped>
#userManagePage {
  padding: 24px;
}

.custom-modal .ant-modal {
  top: 50%;
  transform: translateY(-50%);
}

.ant-form-item {
  margin-bottom: 16px;
}

</style>
