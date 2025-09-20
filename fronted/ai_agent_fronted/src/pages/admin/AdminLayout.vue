<template>
  <div id="admin-layout">
    <div class="sidebar">
      <ul>
        <li :class="{ active: isActive('user') }" @click="activeTab = 'user'">
          <span>用户管理</span>
        </li>
        <li :class="{ active: isActive('app') }" @click="activeTab = 'app'">

          <span>App管理</span>
        </li>
        <li :class="{ active: isActive('chat') }" @click="activeTab = 'chat'">
          <span>对话管理</span>
        </li>
<!--        <li :class="{ active: isActive('analytics') }" @click="activeTab = 'analytics'">-->
<!--          <area-chart-outlined />-->
<!--          <span>数据分析</span>-->
<!--        </li>-->
      </ul>
    </div>

    <div class="content">
      <component :is="currentComponent"></component>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import UserManagePage from './UserManagePage.vue'
import AppManagePage from './AppManagePage.vue'
import ChatManagePage from './ChatManagePage.vue'
// import Analytics from "./Analytics.vue";

const isActive = (tabName: string) => activeTab.value === tabName

const activeTab = ref('user')

const currentComponent = computed(() => {
  switch (activeTab.value) {
    case 'user':
      return UserManagePage
    case 'app':
      return AppManagePage
    case 'chat':
      return ChatManagePage
    // case 'analytics':
    //   return Analytics
    default:
      return UserManagePage
  }
})
</script>

<style scoped>
#admin-layout {
  display: flex;
  height: 100vh;
  font-family: 'Segoe UI', system-ui, sans-serif;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #88b9e7 0%, #bfd1dc 100%);
  padding: 24px 0;
  box-shadow: 3px 0 10px rgba(0, 0, 0, 0.1);
  color: white;
  transition: width 0.3s ease;
}

.sidebar ul {
  padding: 0;
  margin: 0;
}

.sidebar li {
  padding: 14px 32px;
  margin: 8px 16px;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  overflow: hidden;
}

.sidebar li::before {
  content: '';
  position: absolute;
  left: -100%;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  transition: left 0.3s ease;
}

.sidebar li:hover {
  background: rgba(255, 255, 255, 0.05);
  transform: translateX(4px);
}

.sidebar li:hover::before {
  left: 0;
}

.sidebar li.active {
  background: rgba(255, 255, 255, 0.1);
  font-weight: 500;
  box-shadow: inset 3px 0 0 #fff;
}

.content {
  flex: 1;
  padding: 32px;
  background: #f8f9fa;
  overflow-y: auto;
}

.content::-webkit-scrollbar {
  width: 8px;
}

.content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

@media (max-width: 768px) {
  .sidebar {
    width: 72px;
    padding: 24px 8px;
  }

  .sidebar li {
    padding: 14px;
    margin: 8px 4px;
    justify-content: center;
  }

  .sidebar li span {
    display: none;
  }
}
</style>
