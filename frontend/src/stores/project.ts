import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project } from '@/types'
import { projectApi } from '@/api/project'

export const useProjectStore = defineStore('project', () => {
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)

  const fetchProjects = async () => {
    loading.value = true
    try {
      const res = await projectApi.list()
      projects.value = res.data
    } finally {
      loading.value = false
    }
  }

  const fetchProject = async (id: number) => {
    const res = await projectApi.get(id)
    currentProject.value = res.data
    return res.data
  }

  const createProject = async (data: Project) => {
    const res = await projectApi.create(data)
    projects.value.unshift(res.data)
    return res.data
  }

  const updateProject = async (id: number, data: Partial<Project>) => {
    const res = await projectApi.update(id, data)
    const idx = projects.value.findIndex(p => p.id === id)
    if (idx >= 0) projects.value[idx] = res.data
    if (currentProject.value?.id === id) currentProject.value = res.data
    return res.data
  }

  const deleteProject = async (id: number) => {
    await projectApi.delete(id)
    projects.value = projects.value.filter(p => p.id !== id)
    if (currentProject.value?.id === id) currentProject.value = null
  }

  return {
    projects, currentProject, loading,
    fetchProjects, fetchProject, createProject, updateProject, deleteProject
  }
})
