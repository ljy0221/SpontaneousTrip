<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  password: '',
  passwordConfirm: '',
  name: '',
  email: ''
})

const handleJoin = async () => {
  if (form.value.password !== form.value.passwordConfirm) {
    alert('비밀번호가 일치하지 않습니다!')
    return
  }

  try {
    await userStore.register(form.value.email, form.value.password, form.value.name)
    alert('회원가입 성공! 로그인해주세요.')
    router.push('/login')
  } catch (error) {
    console.error('Registration failed:', error)
    alert('회원가입 실패. 다시 시도해주세요.')
  }
}
</script>

<template>
  <div class="container join-container">
    <div class="card join-card">
      <h1 class="title">회원가입</h1>
      <form @submit.prevent="handleJoin">
        <div class="form-group">
          <label class="form-label" for="email">이메일</label>
          <input 
            id="email"
            v-model="form.email" 
            type="email" 
            class="form-input" 
            required
          />
        </div>

        <div class="form-group">
          <label class="form-label" for="password">비밀번호</label>
          <input 
            id="password"
            v-model="form.password" 
            type="password" 
            class="form-input" 
            required
          />
        </div>

        <div class="form-group">
          <label class="form-label" for="passwordConfirm">비밀번호 확인</label>
          <input 
            id="passwordConfirm"
            v-model="form.passwordConfirm" 
            type="password" 
            class="form-input" 
            required
          />
        </div>

        <div class="form-group">
          <label class="form-label" for="name">이름</label>
          <input 
            id="name"
            v-model="form.name" 
            type="text" 
            class="form-input" 
            required
          />
        </div>

        <button type="submit" class="btn btn-primary btn-block">회원가입</button>
      </form>
    </div>
  </div>
</template>

<style lang="scss" scoped>
// Mobile First Styles
.join-container {
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

.join-card {
  width: 100%;
  max-width: 480px;
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
    content: '저희와 함께 여행을 시작해보세요';
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
}

.input-group {
  display: flex;
  gap: 0.5rem;
}

.btn-sm {
  padding: 0.5rem 1rem;
  white-space: nowrap;
}

.btn-block {
  width: 100%;
  margin-top: 1.5rem;
  padding: 0.875rem;
  font-size: var(--font-size-base);
  font-weight: 600;
  background-color: var(--color-cyan);
  border-radius: var(--radius-lg);
  min-height: 48px;

  &:hover {
    background-color: var(--color-cyan-hover);
    transform: translateY(-2px);
  }
}
</style>
