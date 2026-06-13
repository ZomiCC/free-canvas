import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'ProjectList',
      component: () => import('@/views/ProjectList.vue')
    },
    {
      path: '/canvas/:projectId',
      name: 'CanvasEditor',
      component: () => import('@/views/CanvasEditor.vue'),
      props: true
    }
  ]
})

export default router
