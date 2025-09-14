<template>
  <div class="nav-container">
    <div class="header wrapper">
      <!-- 网站标题和logo -->
      <div class="index-title">
        <RouterLink to="/">
          <img src="../assets/favicon.ico" alt="logo" class="logo">
        </RouterLink>
        <h1>AI智能体</h1>
      </div>

      <!-- 日期信息栏 -->
      <div class="date-info">
        {{ fullDateInfo }}
      </div>
    </div>

    <!-- 主导航菜单 -->
    <div class="main-nav">
      <a v-for="(item, index) in navItems"
         :key="index"
         :href="item.link"
         :class="['nav-item', { active: currentNav === index }]"
         @click="handleNavClick(index)">
        {{ item.text }}
      </a>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { Lunar } from 'lunar-javascript'

const currentNav = ref(0)

// 导航菜单项
const navItems = [
  { text: '首页', link: '#' },
  { text: '时空衣橱', link: '#' },
  { text: '霓裳迷阵', link: '#' },
  { text: '千丝万缕', link: '#' },
  { text: '衣冠思辨', link: '#' },
  { text: '华裳千年', link: '#' }
]

// 完整日期信息计算属性
const fullDateInfo = computed(() => {
  const now = new Date()
  const lunar = Lunar.fromDate(now)

  // 公历部分
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')

  // 星期
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  const weekday = `星期${weekdays[now.getDay()]}`

  // 农历部分
  const lunarMonth = lunar.getMonthInChinese()
  const lunarDay = lunar.getDayInChinese()
  const solarTerm = lunar.getJieQi() || ''

  return `${year}-${month}-${date} ${hours}:${minutes} ${weekday} 农历${lunarMonth}月${lunarDay} ${solarTerm}`
})

// 处理导航点击
const handleNavClick = (index: number) => {
  currentNav.value = index
  // 这里可以添加路由跳转逻辑
}

// 初始化更新时间
onMounted(() => {
  setInterval(() => {
    // 通过计算属性自动更新
  }, 1000)
})
</script>

<style scoped>
.nav-container {
  width: 100%;
  background-color: var(--header-bg);
  color: var(--nav-text-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  border-bottom: 1px solid var(--border-color);
}

.index-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo {
  width: 48px;
  height: 48px;
}

.index-title h1 {
  margin: 0;
  font-size: 20px;
  font-weight: 500;
}

.date-info {
  font-size: 14px;
  color: var(--text-secondary);
  white-space: nowrap;
}

.main-nav {
  display: flex;
  justify-content: center;
  padding: 8px 0;
  background-color: var(--nav-bg);
}

.nav-item {
  padding: 12px 24px;
  color: var(--nav-text-color);
  text-decoration: none;
  transition: all 0.3s;
  position: relative;
}

.nav-item:hover {
  color: var(--primary-color);
}

.nav-item.active {
  color: var(--primary-color);
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 60%;
    height: 2px;
    background-color: var(--primary-color);
  }
}
</style>
