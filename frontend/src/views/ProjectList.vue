<template>
  <div class="project-list-page">
    <header class="page-header">
      <h1>🎬 自由画布</h1>
      <p class="subtitle">可视化剧情编辑器 — 将创意变为影像</p>
    </header>

    <div class="toolbar">
      <el-input
        v-model="newProjectName"
        placeholder="输入项目名称..."
        class="name-input"
        @keyup.enter="handleCreate"
      />
      <el-button type="primary" @click="handleCreate" :loading="creating">
        创建新项目
      </el-button>
    </div>

    <div class="project-grid" v-loading="store.loading">
      <el-card
        v-for="project in store.projects"
        :key="project.id"
        class="project-card"
        shadow="hover"
        @click="openProject(project.id!)"
      >
        <div class="card-thumbnail">
          <span class="placeholder-icon">🎞️</span>
        </div>
        <div class="card-info">
          <h3>{{ project.name }}</h3>
          <p>{{ project.description || '暂无描述' }}</p>
          <span class="update-time">{{ formatTime(project.updatedAt) }}</span>
        </div>
        <div class="card-actions">
          <el-button size="small" text @click.stop="editProject(project)">编辑</el-button>
          <el-button size="small" text type="danger" @click.stop="handleDelete(project.id!)">删除</el-button>
        </div>
      </el-card>

      <div v-if="store.projects.length === 0 && !store.loading" class="empty-state">
        <span class="empty-icon">📝</span>
        <p>还没有项目，创建一个开始吧</p>
      </div>
    </div>

    <!-- 编辑对话框 -->
    <el-dialog v-model="editVisible" title="编辑项目">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleEditSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import type { Project } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const store = useProjectStore()

const newProjectName = ref('')
const creating = ref(false)
const editVisible = ref(false)
const editForm = ref<Project>({ name: '', description: '' })
const editingId = ref<number | null>(null)

onMounted(() => {
  store.fetchProjects()
})

const handleCreate = async () => {
  if (!newProjectName.value.trim()) {
    ElMessage.warning('请输入项目名称')
    return
  }
  creating.value = true
  try {
    const project = await store.createProject({
      name: newProjectName.value.trim(),
      description: ''
    })
    newProjectName.value = ''
    router.push(`/canvas/${project.id}`)
  } catch (e: any) {
    ElMessage.error(e.response?.data?.error || '创建失败')
  } finally {
    creating.value = false
  }
}

const openProject = (id: number) => {
  router.push(`/canvas/${id}`)
}

const editProject = (project: Project) => {
  editingId.value = project.id!
  editForm.value = { name: project.name, description: project.description }
  editVisible.value = true
}

const handleEditSave = async () => {
  if (!editingId.value) return
  try {
    await store.updateProject(editingId.value, editForm.value)
    editVisible.value = false
    ElMessage.success('更新成功')
  } catch (e: any) {
    ElMessage.error('更新失败')
  }
}

const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定删除此项目？所有节点和连接将被永久删除。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await store.deleteProject(id)
    ElMessage.success('已删除')
  } catch { /* 取消 */ }
}

const formatTime = (time?: string) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.project-list-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #0f0f1a;
  color: #e0e0e0;
  overflow-y: auto;
}

.page-header {
  text-align: center;
  padding: 40px 20px 20px;
}
.page-header h1 {
  font-size: 2.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 8px;
}
.subtitle { color: #888; font-size: 1rem; }

.toolbar {
  display: flex;
  gap: 12px;
  padding: 0 40px 24px;
  justify-content: center;
}
.name-input { width: 300px; }

.project-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  padding: 0 40px 40px;
}

.project-card {
  background: #1a1a2e;
  border: 1px solid #2a2a4a;
  border-radius: 12px;
  cursor: pointer;
  transition: transform 0.2s, border-color 0.2s;
}
.project-card:hover {
  transform: translateY(-4px);
  border-color: #667eea;
}
.card-thumbnail {
  height: 120px;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px 8px 0 0;
}
.placeholder-icon { font-size: 3rem; opacity: 0.5; }
.card-info { padding: 16px; }
.card-info h3 { font-size: 1.1rem; margin-bottom: 4px; }
.card-info p { color: #888; font-size: 0.85rem; margin-bottom: 8px; }
.update-time { color: #666; font-size: 0.75rem; }
.card-actions { display: flex; justify-content: flex-end; padding: 0 16px 12px; gap: 8px; }

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 80px 20px;
  color: #666;
}
.empty-icon { font-size: 4rem; display: block; margin-bottom: 16px; }
</style>
