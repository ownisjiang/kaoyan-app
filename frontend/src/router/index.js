import { createRouter, createWebHashHistory } from 'vue-router'
import { isLoggedIn } from '../utils/auth.js'

const router = createRouter({
  history: createWebHashHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginPage.vue'),
      meta: { title: '登录', public: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterPage.vue'),
      meta: { title: '注册', public: true }
    },
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomePage.vue'),
      meta: { title: '选择科目' }
    },
    {
      path: '/subject/:id',
      name: 'subject',
      component: () => import('../views/SubjectPage.vue'),
      meta: { title: '考点概览' }
    },
    {
      path: '/quiz',
      name: 'quiz',
      component: () => import('../views/QuizPage.vue'),
      meta: { title: '做题界面' }
    },
    {
      path: '/fill-blank',
      name: 'fillBlank',
      component: () => import('../views/FillBlankPage.vue'),
      meta: { title: '填空题' }
    },
    {
      path: '/comprehensive',
      name: 'comprehensive',
      component: () => import('../views/ComprehensivePage.vue'),
      meta: { title: '综合题' }
    },
    {
      path: '/mock-exam',
      name: 'mockExam',
      component: () => import('../views/MockExamPage.vue'),
      meta: { title: '模考' }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../views/AdminPage.vue'),
      meta: { title: '题库管理', adminOnly: true }
    },
    {
      path: '/wrong-book',
      name: 'wrongBook',
      component: () => import('../views/WrongBookPage.vue'),
      meta: { title: '错题本' }
    },
    {
      path: '/report',
      name: 'report',
      component: () => import('../views/ReportPage.vue'),
      meta: { title: '诊断报告' }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfilePage.vue'),
      meta: { title: '个人主页' }
    }
  ]
})

// 全局路由守卫 —— 未登录拦截
router.beforeEach((to, from, next) => {
  // 公开页面直接放行
  if (to.meta.public) {
    // 已登录用户访问登录页 → 重定向到首页
    if (to.name === 'login' && isLoggedIn()) {
      next({ path: '/' })
      return
    }
    next()
    return
  }

  // 需要登录的页面
  if (!isLoggedIn()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  next()
})

export default router
