<template>
  <div id="globalHeader">
    <!-- 顶部栏 -->
    <a-row :wrap="false" class="header-top">
      <a-col>
        <RouterLink to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/favicon.ico" alt="logo" />
            <div class="title-group">
              <div class="main-title">AI智能体</div>
            </div>
          </div>
        </RouterLink>
      </a-col>

      <a-col flex="auto" class="date-info">
        <span v-if="shouldShowFullDate">{{ fullDateInfo }}</span>
      </a-col>

      <a-col class="header-right">
        <div class="notification-btn" @mouseenter="stopPolling" @mouseleave="resumePolling">
          <a-badge :count="unreadCount">
            <a-button @click="showNotificationDrawer" class="bell-btn" shape="circle">
              <template #icon>
                <BellOutlined style="color: var(--nav-icon-color)" />
              </template>
            </a-button>
          </a-badge>
        </div>

        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser?.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                <span class="user-name">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="navigateToUserProfile">
                    <UserOutlined style="color: var(--nav-icon-color)" />
                    用户中心
                  </a-menu-item>

                  <a-menu-item
                    v-if="loginUserStore.isAdmin"
                    key="userManage"
                    @click="navigateToUserManage"
                  >
                    <SettingOutlined style="color: var(--nav-icon-color)" />
                    后台管理
                  </a-menu-item>

                  <a-menu-item @click="doLogout">
                    <LogoutOutlined style="color: var(--nav-icon-color)" />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" @click="navigateToLogin">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 导航栏 -->
    <a-row class="nav-bottom">
      <a-col flex="auto" class="nav-container">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :theme="themeMode"
          :items="filteredItems"
          :overflowedIndicator="null"
          :overflowedIndicatorPopupClassName="null"
          @click="doMenuClick"
        >
          <template #itemIcon="{ icon }">
            <component :is="icon" style="color: var(--nav-icon-color)" />
          </template>
        </a-menu>
      </a-col>
    </a-row>

    <!-- 通知抽屉 -->
    <a-drawer
      v-model:open="openNotification"
      title="通知中心"
      placement="right"
      width="360"
      :closable="true"
    >
      <div v-if="loading">加载中...</div>
      <div v-else-if="errorMessage">
        <div class="error-message">{{ errorMessage }}</div>
      </div>
      <div v-else-if="notifications.length === 0">
        <div class="no-notice">暂无通知</div>
      </div>
      <div class="notification-list" v-else>
        <a-card
          v-for="notification in notifications"
          :key="notification.id"
          class="notification-item"
          :class="{ unread: !notification.read, read: notification.read }"
        >
          <template #title>
            <div class="card-title">{{ notification.title }}</div>
          </template>
          <template #default>
            <div class="card-content">
              {{ safeText(notification.content!) }}
            </div>
          </template>
          <template #extra>
            <div class="card-footer">
              <div class="footer-left">{{ formatDate(notification.createTime) }}</div>
              <div class="footer-right">
                <a-button
                  v-if="!notification.read"
                  type="text"
                  @click="handleMarkRead(notification.id)"
                >
                  <CheckOutlined />
                  已读
                </a-button>
                <a-button
                  v-if="notification.read"
                  type="text"
                  @click="handleDelete(notification.id)"
                >
                  <DeleteOutlined />
                  删除
                </a-button>
              </div>
            </div>
          </template>
        </a-card>
      </div>
    </a-drawer>
  </div>
</template>

<script lang="ts" setup>
import { h, ref, onMounted, onUnmounted, computed, watch } from 'vue'
import {
  HomeOutlined,
  LogoutOutlined,
  UserOutlined,
  CheckOutlined,
  DeleteOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import { message, notification } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { userLogout } from '@/api/userController.ts'
import type { MenuProps } from 'ant-design-vue/lib'
import { BellOutlined } from '@ant-design/icons-vue'
import {
  deleteMessage,
  getMessages,
  markAsRead,
} from '@/api/messageController.ts'
import { NOTIFICATION_READ_STATUS, type NotificationReadStatus } from '@/constants/isRead.ts'

import { useThemeStore } from '@/stores/useThemeStore'
import { Lunar } from 'lunar-javascript'
import permission from '@/directives/permission'

const themeStore = useThemeStore()
const themeMode = computed(() => {
  return themeStore.currentTheme === 'dark' ? 'dark' : 'light'
})

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()
const userId = computed(() => loginUserStore.loginUser?.id)

defineOptions({
  directives: {
    permission,
  },
})
const navigateToUserManage = () => {
  router.push('/admin/AdminLayout')
}

const shouldShowFullDate = ref(true)
const handleResize = () => {
  shouldShowFullDate.value = window.innerWidth > 1000
}
onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
const fullDateInfo = computed(() => {
  const now = new Date()
  const lunar = Lunar.fromDate(now)

  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')

  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const weekday = `星期${weekdays[now.getDay()]}`

  const lunarMonth = lunar.getMonthInChinese()
  const lunarDay = lunar.getDayInChinese()
  const solarTerm = lunar.getJieQi() || ''

  return `${year}-${month}-${date} ${hours}:${minutes} ${weekday} 农历${lunarMonth}月${lunarDay} ${solarTerm}`
})

const current = ref<string[]>(['/'])
const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: '/waterfall',
    label: 'item2',
    title: 'item2',
  },
  {
    key: '/diy',
    label: 'item3',
    title: 'item3',
  },
  {
    key: '/contact',
    label: 'item4',
    title: 'item4',
  },
  {
    key: '/AnalyseMap',
    label: 'item5',
    title: 'item5',
  },
])
// 过滤菜单项
const filterMenus = (menus?: MenuProps['items']) => {
  return (menus || []).filter((menu) => {
    if (menu && typeof menu.key === 'string' && menu.key.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      return loginUser && Array.isArray(loginUser.roles) && loginUser.roles.includes('ADMIN')
    }
    return true
  })
}

// 展示在菜单的路由数组
const filteredItems = computed(() => filterMenus(items.value))

const router = useRouter()
const removeRouteGuard = router.afterEach((to) => {
  const normalizedPath = to.path.replace(/\/+$/, '').toLowerCase()
  current.value = [normalizedPath]
})

// 路由跳转事件
const doMenuClick = async ({ key }: { key: string }) => {
  try {
    await router.push({ path: key })
  } catch (error) {
    console.error('路由跳转失败:', error)
    message.error('路由跳转失败，请稍后再试')
  }
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  console.log(res)
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({ userName: '未登录' })
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.description)
  }
}

// 导航到登录页面
const navigateToLogin = () => {
  router.push('/user/login')
}

// 导航到用户中心页面
const navigateToUserProfile = () => {
  router.push('/user/profile')
}

// 监听登录状态变化
watch(userId, (newVal) => {
  if (newVal) {
    connectSSE()
    fetchMessages()
  } else {
    disconnectSSE()
  }
})

const pollIntervalTime = ref(60000) // 短轮询间隔（1分钟）
let pollInterval: number | undefined = undefined
let safePollInterval: number | undefined = undefined // 长轮询备用（5分钟）

// 重连机制相关
const MAX_RECONNECT_ATTEMPTS = 5
let reconnectAttempts = 0
let eventSource: EventSource | null = null
const isSSEActive = ref(false)

// 网络状态监听
onMounted(() => {
  window.addEventListener('online', handleOnline)
})

onUnmounted(() => {
  window.removeEventListener('online', handleOnline)
})

const connectSSE = () => {
  if (!userId.value) return
  eventSource = new EventSource(
    `${import.meta.env.VITE_APP_BASEURL}/api/sse/connect?userId=${userId.value}`,
  )

  eventSource.onopen = () => {
    isSSEActive.value = true
    stopPolling()
  }

  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data)
    handleNewMessage(data)
  }

  eventSource.onerror = (err) => {
    console.error('SSE连接失败:', err)
    reconnectAttempts++
    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      const delay = 2 ** reconnectAttempts * 1000
      setTimeout(() => connectSSE(), delay)
    } else {
      disconnectSSE()
    }
  }
}

const disconnectSSE = () => {
  eventSource?.close()
  eventSource = null
  isSSEActive.value = false
  reconnectAttempts = 0
  resumePolling()
}

// 轮询控制
const stopPolling = () => {
  if (pollInterval) window.clearInterval(pollInterval)
  if (safePollInterval) window.clearInterval(safePollInterval)
  pollInterval = undefined
  safePollInterval = undefined
}

const resumePolling = () => {
  if (!pollInterval) {
    pollInterval = window.setInterval(fetchMessages, pollIntervalTime.value)
  }
  if (!safePollInterval) {
    safePollInterval = window.setInterval(fetchMessages, 300000)
  }
}

// 网络恢复时重连
const handleOnline = () => {
  if (!eventSource && userId.value) {
    connectSSE()
  }
}

// 处理新消息
const handleNewMessage = (messageData: API.Message) => {
  const existing = notifications.value.find((n) => n.id === messageData.id)
  if (!existing) {
    notifications.value.unshift({
      ...messageData,
      read: false,
      isRead: NOTIFICATION_READ_STATUS.UNREAD,
    })
  }

  // 触发全局通知弹窗
  notification.info({
    message: '新消息',
    description: messageData.content,
    duration: 3,
  })
}

// 通知相关逻辑
interface NotificationItem extends API.Message {
  read: boolean
  isRead: NotificationReadStatus
}

const notifications = ref<NotificationItem[]>([])
const loading = ref<boolean>(false)
const errorMessage = ref<string | null>(null)

// 抽屉状态
const openNotification = ref<boolean>(false)

// 请求控制器
let abortController: AbortController | null = null

// 安全文本处理
const safeText = (text: string | undefined): string => {
  return text ? text.replace(/</g, '&lt;').replace(/>/g, '&gt;') : ''
}

// 数据获取函数
const fetchMessages = async () => {
  try {
    abortController?.abort() // 取消前一个请求
    abortController = new AbortController()
    const signal = abortController.signal
    const response = await getMessages({ signal })

    if (response.data.code === 0 && Array.isArray(response.data.data)) {
      notifications.value = response.data.data.map((item) => ({
        ...item,
        read: item.isRead === NOTIFICATION_READ_STATUS.READ,
      })) as NotificationItem[]
      errorMessage.value = null
    } else {
      errorMessage.value = '获取消息失败，请稍后再试'
    }
  } catch (error: any) {
    if (error.name !== 'AbortError') {
      errorMessage.value = '请求消息时出错，请检查网络连接'
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loading.value = true
  fetchMessages()
  resumePolling() // 同时启动短/长轮询
  connectSSE() // 尝试初始连接
})

onUnmounted(() => {
  removeRouteGuard() // 清除路由监听器
  if (abortController) abortController.abort()
  clearInterval(pollInterval) //清除轮询
  stopPolling()
  disconnectSSE() // 清除SSE连接
})

// 抽屉显示逻辑
const showNotificationDrawer = () => {
  openNotification.value = true
}

// 未读计数计算属性
const unreadCount = computed(() => notifications.value.filter((n) => !n.read).length)

// 删除消息
const handleDelete = async (messageId: number) => {
  try {
    await deleteMessage({ messageId })
    notifications.value = notifications.value.filter((n) => n.id !== messageId)
    message.success('删除成功')
  } catch (error) {
    message.error('删除失败，请重试')
  }
}

// 标记已读
const handleMarkRead = async (messageId: number) => {
  try {
    await markAsRead({ messageId })
    const index = notifications.value.findIndex((n) => n.id === messageId)
    if (index !== -1) {
      notifications.value[index].read = true
      notifications.value[index].isRead = NOTIFICATION_READ_STATUS.READ
    }
    message.success('标记成功')
  } catch (error) {
    message.error('标记失败，请重试')
  }
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')

  return `${year}/${String(month).padStart(2, '0')}/${String(day).padStart(2, '0')} ${hours}:${minutes}`
}
</script>

<style scoped>
#globalHeader {
  width: 100%;
  z-index: 1000;
  background-color: var(--header-bg);

  & > * + * {
    margin-top: 8px;
  }
}

.header-top {
  display: flex;
  align-items: center;
  padding: 12px 32px;
  width: 100%;
  box-sizing: border-box;
  border-bottom: 1px solid var(--divider-color);
  height: 60px;
  line-height: 60px;
  background-color: transparent;
}

.nav-bottom {
  height: 56px;
  margin: 0 !important;
  padding: 0 16px;
  background-color: transparent;
  box-sizing: border-box;
  display: flex;
  align-items: center;
  line-height: 56px;
}

.nav-container {
  display: flex;
  justify-content: center;
  overflow-x: auto;
  white-space: nowrap;
  -ms-overflow-style: none;
  scrollbar-width: none;
  width: 100%;
}

.nav-container::-webkit-scrollbar {
  display: none;
}

.title-bar {
  display: flex;
  align-items: center;
  transition: opacity 0.3s;
}

.title-bar:hover {
  opacity: 0.8;
}

.logo {
  height: 48px;
  margin-right: 12px;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
}

.main-title {
  color: var(--nav-text-color);
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.date-info {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  color: var(--text-secondary);
  font-size: 15px;
  padding: 0 24px;
  font-family: monospace;
  margin-left: auto;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 16px;
}

.notification-btn {
  position: relative;
}

.bell-btn {
  padding: 6px;
  border: none;
  background: none;
  transition: transform 0.3s ease;
}

.bell-btn:hover {
  transform: scale(1.2);
  filter: brightness(1.2);
}

.user-login-status {
  display: flex;
  align-items: center;
}

.user-name {
  margin-left: 8px;
  font-weight: 500;
  color: var(--nav-text-color);
}

::v-deep(.ant-menu-horizontal) {
  margin: 0 !important;
  padding: 0 !important;
  border-bottom: none !important;
  overflow: visible !important;
}

::v-deep(.ant-menu-overflow) {
  display: flex !important;
  flex-wrap: nowrap !important;
  overflow: visible !important;
}

::v-deep(.ant-menu-item) {
  flex-shrink: 0;
  padding: 0 24px !important;
  font-size: 16px !important;
  display: flex !important;
  align-items: center !important;
  transition: all 0.3s ease !important;
  border-radius: 6px !important;
  margin: 0 4px !important;
  white-space: nowrap;
}

::v-deep(.ant-menu-item:hover) {
  color: var(--nav-active-color) !important;
  background: rgba(255, 255, 255, 0.08) !important;
  transform: translateY(-2px);
}

::v-deep(.ant-menu-item-selected) {
  color: var(--nav-active-color) !important;
  border-top: 5px solid var(--nav-active-color);
  border-bottom: none !important;
  box-sizing: border-box;
  padding: 0 24px !important;
}

::v-deep(.ant-menu-item-icon) {
  margin-right: 10px !important;
  font-size: 20px !important;
  transition: transform 0.3s;
}

::v-deep(.ant-menu-item:hover .ant-menu-item-icon) {
  transform: scale(1.1);
}

/* 主题变量 */
:root {
  --nav-active-color: #1890ff;
  --divider-color: rgba(255, 255, 255, 0.12);
  --nav-text-color: rgba(255, 255, 255, 0.9);
  --nav-icon-color: rgba(255, 255, 255, 0.75);
  --header-bg: #1f1f1f;
  --text-secondary: rgba(255, 255, 255, 0.65);
}

[data-theme='dark'] {
  --header-bg: #2d3436;
  --divider-color: rgba(255, 255, 255, 0.08);
}

[data-theme='light'] {
  --header-bg: #ffffff;
  --nav-text-color: rgba(0, 0, 0, 0.85);
  --nav-icon-color: rgba(0, 0, 0, 0.65);
  --divider-color: rgba(0, 0, 0, 0.06);
  --text-secondary: rgba(0, 0, 0, 0.45);
}
</style>
