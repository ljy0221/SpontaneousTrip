<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const id = ref('')
const password = ref('')
const rememberMe = ref(false)

const handleLogin = async () => {
  if (!id.value || !password.value) {
    alert('아이디와 비밀번호를 모두 입력해주세요.')
    return
  }

  try {
    await authStore.login(id.value, password.value, rememberMe.value)
    router.push('/')
  } catch (error) {
    alert('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.')
  }
}
</script>

<template>
  <div class="container login-container">
    <div class="card login-card">
      <h1 class="title">로그인</h1>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label class="form-label" for="id">아이디</label>
          <input 
            id="id"
            v-model="id" 
            type="text" 
            class="form-input" 
            placeholder="아이디를 입력하세요" 
            required
          />
        </div>
        <div class="form-group">
          <label class="form-label" for="password">비밀번호</label>
          <input 
            id="password"
            v-model="password" 
            type="password" 
            class="form-input" 
            placeholder="비밀번호를 입력하세요" 
            required
          />
        </div>
        <div class="form-group checkbox-group">
          <input 
            type="checkbox" 
            id="remember" 
            v-model="rememberMe"
          >
          <label for="remember">로그인 상태 유지</label>
        </div>
        <button type="submit" class="btn btn-primary btn-block">로그인</button>
        
        <div class="links">
          <router-link to="/join" class="link">회원가입</router-link>
          <span class="divider">|</span>
          <router-link to="/find-password" class="link">비밀번호 찾기</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// Mobile First Styles
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 75vh;
  padding: var(--spacing-lg) var(--spacing-md);
  background: linear-gradient(135deg, #F5F1E8 0%, #E8EAF6 100%);
  position: relative;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: radial-gradient(circle at 20% 50%, rgba(34, 211, 238, 0.05) 0%, transparent 50%),
                      radial-gradient(circle at 80% 80%, rgba(79, 70, 229, 0.05) 0%, transparent 50%);
    pointer-events: none;
  }
  
  @media (min-width: 768px) {
    padding: var(--spacing-xl) 0;
  }
}

.login-card {
  width: 100%;
  max-width: 440px;
  padding: 2rem;
  background-color: var(--color-surface);
  box-shadow: var(--shadow-lg);
  border-radius: var(--radius-lg);
  position: relative;
  z-index: 1;
  
  @media (min-width: 768px) {
    padding: 3rem;
    box-shadow: var(--shadow-xl);
    border-radius: var(--radius-xl);
  }
}

.title {
  text-align: center;
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--color-text-main);
  letter-spacing: -0.025em;

  &::after {
    content: '모험을 떠날 준비가 되셨나요?';
    display: block;
    font-size: var(--font-size-sm);
    font-weight: 500;
    color: var(--color-text-muted);
    margin-top: 0.5rem;
    letter-spacing: normal;
    
    @media (min-width: 768px) {
      font-size: var(--font-size-base);
    }
  }
  
  @media (min-width: 768px) {
    font-size: var(--font-size-3xl);
  }
}

form {
  margin-top: 1.5rem;
  
  @media (min-width: 768px) {
    margin-top: 2rem;
  }
}

.form-group {
  margin-bottom: 1.25rem;

  &.checkbox-group {
    display: flex;
    align-items: center;
    margin-bottom: 1.5rem;
    gap: 0.5rem;

    input[type="checkbox"] {
      width: 1.125rem;
      height: 1.125rem;
      cursor: pointer;
      accent-color: var(--color-cyan);
    }

    label {
      font-size: var(--font-size-sm);
      color: var(--color-text-main);
      font-weight: 500;
      cursor: pointer;
      user-select: none;
      margin-bottom: 0;
    }
  }
}

.btn-block {
  width: 100%;
  margin-top: 1.5rem;
  padding: 0.875rem;
  font-size: var(--font-size-base);
  font-weight: 600;
  background-color: var(--color-cyan);
  border-radius: var(--radius-lg);
  
  // Larger touch target for mobile
  min-height: 48px;

  &:hover {
    background-color: var(--color-cyan-hover);
    transform: translateY(-2px);
  }
}

.links {
  margin-top: 1.5rem;
  text-align: center;
  font-size: var(--font-size-sm);
  padding-top: 1.5rem;
  border-top: 1px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    margin-top: 2rem;
  }
}

.link {
  color: var(--color-cyan);
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s ease;
  
  // Larger touch target for mobile
  display: inline-block;
  padding: 0.25rem 0.5rem;
  margin: -0.25rem -0.5rem;

  &:hover {
    color: var(--color-cyan-hover);
    text-decoration: underline;
  }
}

.divider {
  margin: 0 0.5rem;
  color: var(--color-border);
  
  @media (min-width: 768px) {
    margin: 0 0.75rem;
  }
}
</style>
