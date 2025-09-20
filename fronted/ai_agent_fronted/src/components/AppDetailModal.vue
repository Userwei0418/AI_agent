<template>
  <a-modal v-model:open="visible" title="应用详情" :footer="null" width="500px">
    <div class="app-detail-content">
      <!-- 应用基础信息 -->
      <div class="app-basic-info">
        <div class="info-item">
          <span class="info-label">创建者：</span>
          <UserInfo :user="app?.user" size="small" />
        </div>
        <div class="info-item">
          <span class="info-label">创建时间：</span>
          <span>{{ formattedCreateTime }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">生成类型：</span>
          <a-tag v-if="app?.codeGenType" color="blue">
            {{ formatCodeGenType(app.codeGenType) }}
          </a-tag>
          <span v-else>未知类型</span>
        </div>
      </div>

      <!-- 操作栏（仅本人或管理员可见） -->
      <div v-if="showActions" class="app-actions">
        <a-space>
          <a-button type="primary" @click="handleEdit">
            <template #icon>
              <EditOutlined />
            </template>
            修改
          </a-button>
          <a-popconfirm
            title="确定要删除这个应用吗？"
            @confirm="handleDelete"
            ok-text="确定"
            cancel-text="取消"
          >
            <a-button danger>
              <template #icon>
                <DeleteOutlined />
              </template>
              删除
            </a-button>
          </a-popconfirm>
        </a-space>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import UserInfo from './UserInfo.vue'
import { formatCodeGenType } from '@/utils/codeGenTypes'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

// 添加时间解析函数
const parseDateTimeArray = (dateArray: number[] | undefined): Date | null => {
  if (!dateArray || dateArray.length < 6) {
    return null
  }
  // 注意：JavaScript 中月份是从0开始的，所以需要减1
  return new Date(
    dateArray[0],     // 年
    dateArray[1] - 1, // 月（需要减1）
    dateArray[2],     // 日
    dateArray[3],     // 时
    dateArray[4],     // 分
    dateArray[5]      // 秒
  )
}

// 格式化时间显示的函数
const formatDateTime = (date: Date | null): string => {
  if (!date || isNaN(date.getTime())) {
    return '未知时间'
  }
  return date.toLocaleString('zh-CN')
}

const props = withDefaults(defineProps<Props>(), {
  showActions: false,
})

const emit = defineEmits<Emits>()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

// 计算格式化后的创建时间
const formattedCreateTime = computed(() => {
  if (!props.app?.createTime) {
    return '未知时间'
  }

  // 如果是数组格式的时间
  if (Array.isArray(props.app.createTime)) {
    const date = parseDateTimeArray(props.app.createTime as unknown as number[])
    return formatDateTime(date)
  }

  // 如果是字符串格式的时间，直接返回
  return props.app.createTime
})

const handleEdit = () => {
  emit('edit')
}

const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.app-detail-content {
  padding: 8px 0;
}

.app-basic-info {
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.info-label {
  width: 80px;
  color: #666;
  font-size: 14px;
  flex-shrink: 0;
}

.app-actions {
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>
