<template>
  <div id="basicLayout" :data-theme="theme">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
        <!--        <HeaderDemo />-->
      </a-layout-header>

      <a-layout-content class="content">
        <router-view />
      </a-layout-content>

      <a-layout-footer class="footer">
        <a href="http://www.zwnsyw.top" target="_blank"> © 2025 Zwww </a>
      </a-layout-footer>
    </a-layout>

    <!-- 主浮动按钮组 -->
    <a-float-button-group trigger="hover" :style="{ right: '24px', bottom: '190px' }">
      <template #icon>
        <menu-outlined />
      </template>

      <!-- 功能按钮 -->
      <a-float-button
        v-for="(item, index) in menuItems"
        :key="index"
        :style="{ right: `${24 + index * 70}px` }"
        :tooltip="item.tooltip"
        @click="handleButtonClick(item.action)"
      >
        <template #icon>
          <component :is="item.icon" />
        </template>
      </a-float-button>

    </a-float-button-group>

    <!-- 返回顶部按钮 -->
    <a-tooltip title="返回顶部">
      <a-back-top :visibility-height="200" :style="{ right: '24px', bottom: '138px' }">
        <a-icon type="vertical-align-top" />
      </a-back-top>
    </a-tooltip>

  </div>

</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import {
  MenuOutlined,
  CustomerServiceOutlined,
  SkinOutlined,
  BookOutlined, ReadOutlined,
} from '@ant-design/icons-vue'
import { ref } from 'vue'


// 菜单配置
const menuItems = ref([
  {
    icon: BookOutlined,
    tooltip: '个人简历',
    action: 'resume'
  },
  {
    icon: ReadOutlined,
    tooltip: '个人博客',
    action: 'blog'
  },
  {
    icon: SkinOutlined,
    tooltip: '切换主题',
    action: 'toggleTheme'
  }
])

// 统一处理按钮点击事件
const handleButtonClick = (action: string) => {
  // 根据不同的 action 执行对应的操作
  switch (action) {
    case 'toggleTheme':
      toggleTheme(action)
      break
    case 'support':
      handleAction(action)
      break
    case 'resume':
      window.open('http://8.130.142.189:20495', '_blank')
      break
    case 'blog':
      window.open('http://8.130.142.189:8080/share/about', '_blank')
      break
    default:
      console.log(`Unknown action: ${action}`)
  }
}

// 切换主题
import { useThemeStore } from '@/stores/useThemeStore'

const themeStore = useThemeStore()
const theme = ref('light')
const toggleTheme = (action: string) => {
  if (action === 'toggleTheme') {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    themeStore.setTheme(themeStore.currentTheme === 'light' ? 'dark' : 'light')
  }
}

//联系我们
const contactDialogVisible = ref(false)
const activeTab = ref(0);
const handleAction = (action: string) => {
  if (action === 'support') {
    contactDialogVisible.value = true
  }
}

// ------------------ 在线客服 ------------------
// https://go.chatway.app/
const scriptContent = `(function() {
  var script = document.createElement('script');
  script.id = 'chatway';
  script.async = true;
  script.src = 'https://cdn.chatway.app/widget.js?id=s6yd5dmvfdYT';
  document.head.appendChild(script);
})();`

// 在组件挂载时执行脚本
import { onMounted } from 'vue'
onMounted(() => {
  eval(scriptContent)
})


// ------------------ 随行导师 ------------------
// https://docs.dify.ai/zh-hans/guides/application-publishing/embedding-in-websites
declare global {
  interface Window {
    difyChatbotConfig: {
      token: string;
      draggable?: boolean;
      dragAxis?: 'x' | 'y' | 'both';
      containerProps?: {
        style?: Record<string, any>;
      };
    };
  }
}

window.difyChatbotConfig = {
  token: 'EoELI94swigw1ATu',
  draggable: true,
  dragAxis: 'x',
  containerProps: {
    style: {
      bottom: '24px',
      right: '24px',
      width: '45px',
      height: '45px',
      borderRadius: '30px',
      backgroundColor: '#c2dfe1',
      boxShadow: '0 4px 8px rgba(0, 0, 0, 0.2)',
      transition: 'transform 0.2s ease'
    },
  },
}

onMounted(() => {
  const script = document.createElement('script')
  script.src = 'https://udify.app/embed.min.js'
  script.id = 'EoELI94swigw1ATu'
  script.defer = true
  document.head.appendChild(script)
})



</script>
<style scoped>
/* 公共基础样式 */
#basicLayout .header,
#basicLayout .content,
#basicLayout .footer {
  transition: all 0.3s ease-in-out;
}

/* 浅色主题 */
#basicLayout[data-theme='light'] {
  --header-bg: #ffffff;
  --content-bg: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  --footer-bg: #f1f3f5;
  --text-primary: #2d3436;
  --text-secondary: #636e72;
  --nav-text-color: #2d3436;
  --nav-icon-color: #2d3436;
  --menu-item-text-color: #2d3436;
  --menu-item-bg-color: #f5f5f5;
}

/* 深色主题 */
#basicLayout[data-theme='dark'] {
  --header-bg: #2d3436;
  --content-bg: linear-gradient(135deg, #3a3a3a 0%, #2d3436 100%);
  --footer-bg: #2d3436;
  --text-primary: #f8f9fa;
  --text-secondary: #adb5bd;
  --nav-text-color: #f8f9fa;
  --nav-icon-color: #f8f9fa;
  --menu-item-text-color: #f8f9fa;
  --menu-item-bg-color: #3a3a3a;
}

/* 头部样式 */
.header {
  background: var(--header-bg);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1;
}

/* 内容区域 */
.content {
  background: var(--content-bg);
  color: var(--text-primary);
  min-height: calc(100vh - 120px);
}

/* 底部样式 */
.footer {
  background: var(--footer-bg);
  color: var(--text-primary);
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
  padding: 1rem 0;
}

.footer a {
  color: var(--text-secondary);
  transition: color 0.2s;
}

.footer a:hover {
  color: var(--text-primary);
}

.ant-float-button-group {
  --button-size: 48px;
  --button-bg: var(--header-bg);
  --button-color: var(--text-primary);
}

.ant-float-button {
  width: var(--button-size);
  height: var(--button-size);
  background: var(--button-bg);
  color: var(--button-color);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s, box-shadow 0.2s;
}

.ant-float-button:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
}

/* 返回顶部按钮 */
.ant-back-top {
  background-color: var(--text-primary);
  color: var(--header-bg);
}

.contact-dialog-content {
  text-align: center;
  padding: 10px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.close {
  cursor: pointer;
}

.main {
  max-width: 400px;
  margin: 0 auto;
}

.code {
  max-width: 100%;
  border-radius: 8px;
}

/* 覆盖 Dify 按钮样式 */
#dify-chatbot-bubble-button {
  --dify-chatbot-bubble-button-border-radius: 30px;
  --dify-chatbot-bubble-button-box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

/* 调整聊天窗口尺寸 */
#dify-chatbot-bubble-window {
  width: 24rem !important;
  height: 40rem !important;
}

/* 浅色模式下的 ant-layout-header 样式
   background: #ffffff !important;
 */
#basicLayout[data-theme='light'] :deep(.ant-layout-header) {
  height: auto !important;
  width: 100% !important;
  padding-inline: 50px !important;
  color: rgba(0, 0, 0, 0.88) !important;
  line-height: normal !important;
  background: #ffffff !important;
}

/* 深色模式下的 ant-layout-header 样式
  background-color: transparent;
  */
#basicLayout[data-theme='dark'] :deep(.ant-layout-header) {
  height: auto !important;
  width: 100% !important;
  padding-inline: 50px !important;
  color: rgba(0, 0, 0, 0.88) !important;
  line-height: normal !important;
  background: #2d3436 !important;
}

[data-theme="dark"] ::v-deep(.ant-menu-dark) {
  background: #2d3436 !important;
  color: rgba(255, 255, 255, 0.65) !important;
}

</style>
