import { createRouter, createWebHistory } from 'vue-router'
import MainView from '../views/MainView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'main',
      component: MainView
    },
    {
      path: '/plan',
      name: 'plan',
      component: () => import('../views/PlanView.vue')
    },
    {
      path: '/hotplace',
      redirect: '/plan'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/join',
      name: 'join',
      component: () => import('../views/JoinView.vue')
    },
    {
      path: '/find-password',
      name: 'find-password',
      component: () => import('../views/FindPassword.vue')
    },
    {
      path: '/board',
      name: 'board',
      component: () => import('../views/BoardListView.vue')
    },
    {
      path: '/board/:id',
      name: 'board-detail',
      component: () => import('../views/BoardDetailView.vue')
    },
    {
      path: '/board/write',
      name: 'board-write',
      component: () => import('../views/BoardWriteView.vue')
    },
    {
      path: '/board/update/:id',
      name: 'board-update',
      component: () => import('../views/BoardUpdateView.vue')
    },
    {
      path: '/mypage',
      name: 'mypage',
      component: () => import('../views/MyPageView.vue')
    },
    {
      path: '/plan/:planId',
      name: 'plan-detail',
      component: () => import('../views/PlanDetailView.vue')
    }
  ]
})

export default router
