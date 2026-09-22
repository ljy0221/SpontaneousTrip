<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const mobileMenuOpen = ref(false)
const navRef = ref(null)

const toggleMobileMenu = () => {
  mobileMenuOpen.value = !mobileMenuOpen.value
}

const closeMobileMenu = () => {
  mobileMenuOpen.value = false
}

// Close menu when clicking outside
const handleClickOutside = (event) => {
  if (mobileMenuOpen.value && navRef.value && !navRef.value.contains(event.target)) {
    const hamburgerBtn = event.target.closest('.hamburger-btn')
    if (!hamburgerBtn) {
      closeMobileMenu()
    }
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <header class="app-header">
    <div class="container header-content">
      <RouterLink to="/" class="logo" @click="closeMobileMenu">
        <img src="/logo.png" alt="Travel Planner" class="logo-img" />
      </RouterLink>
      
      <!-- Hamburger Button (Mobile Only) -->
      <button 
        class="hamburger-btn" 
        @click="toggleMobileMenu"
        :class="{ active: mobileMenuOpen }"
        aria-label="Toggle menu"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>
      
      <!-- Navigation -->
      <nav ref="navRef" :class="{ open: mobileMenuOpen }">
        <template v-if="authStore.isAuthenticated">
          <RouterLink to="/plan" class="nav-link" @click="closeMobileMenu">여행 계획</RouterLink>
          <RouterLink to="/board" class="nav-link" @click="closeMobileMenu">게시판</RouterLink>
          <RouterLink to="/mypage" class="nav-link" @click="closeMobileMenu">마이페이지</RouterLink>
          <span v-if="authStore.nickname" class="nav-text">환영합니다, {{ authStore.nickname }}님!</span>
          <a href="#" @click.prevent="authStore.logout(); closeMobileMenu()" class="nav-link">로그아웃</a>
        </template>
        <template v-else>
          <RouterLink to="/plan" class="nav-link" @click="closeMobileMenu">여행 계획</RouterLink>
          <RouterLink to="/board" class="nav-link" @click="closeMobileMenu">게시판</RouterLink>
          <RouterLink to="/login" class="nav-link" @click="closeMobileMenu">로그인</RouterLink>
          <RouterLink to="/join" class="nav-link" @click="closeMobileMenu">회원가입</RouterLink>
        </template>
      </nav>
    </div>
  </header>
</template>

<style lang="scss" scoped>
// Mobile First Styles with Hamburger Menu
.app-header {
  background-color: var(--color-surface);
  box-shadow: var(--shadow-md);
  padding: 0.875rem 0;
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid var(--color-border-light);
  
  @media (min-width: 768px) {
    padding: 1rem 0;
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.logo {
  display: flex;
  align-items: center;
  transition: opacity 0.2s ease;
  z-index: 101;

  &:hover {
    opacity: 0.8;
  }
  
  .logo-img {
    height: 2.5rem;
    width: auto;
    object-fit: contain;
    
    @media (min-width: 768px) {
      height: 3rem;
    }
  }
}

// Hamburger Button (Mobile Only)
.hamburger-btn {
  display: flex;
  flex-direction: column;
  justify-content: space-around;
  width: 2.5rem;
  height: 2.5rem;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  z-index: 101;
  
  span {
    width: 100%;
    height: 3px;
    background-color: var(--color-cyan);
    border-radius: 10px;
    transition: all 0.3s ease;
    transform-origin: center;
  }
  
  &.active {
    span:nth-child(1) {
      transform: translateY(7px) rotate(45deg);
    }
    
    span:nth-child(2) {
      opacity: 0;
      transform: translateX(-20px);
    }
    
    span:nth-child(3) {
      transform: translateY(-7px) rotate(-45deg);
    }
  }
  
  @media (min-width: 768px) {
    display: none;
  }
}

// Mobile Navigation
nav {
  // Mobile: Drawer menu
  position: fixed;
  top: 0;
  right: -100%;
  width: 75%;
  max-width: 300px;
  height: 100vh;
  background-color: var(--color-surface);
  box-shadow: var(--shadow-xl);
  padding: 5rem 1.5rem 2rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  transition: right 0.3s ease;
  overflow-y: auto;
  z-index: 100;
  
  &.open {
    right: 0;
  }
  
  // Tablet and up: Horizontal menu
  @media (min-width: 768px) {
    position: static;
    width: auto;
    max-width: none;
    height: auto;
    padding: 0;
    flex-direction: row;
    align-items: center;
    gap: 0.5rem;
    box-shadow: none;
    overflow-y: visible;
  }
}

.nav-link {
  padding: 0.875rem 1rem;
  font-weight: 600;
  font-size: var(--font-size-base);
  color: var(--color-text-muted);
  border-radius: var(--radius-md);
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  align-items: center;
  min-height: 48px;
  
  @media (min-width: 768px) {
    padding: 0.5rem 1rem;
    font-size: var(--font-size-sm);
  }

  &:hover {
    color: var(--color-cyan);
    background-color: var(--color-cyan-light);
  }

  &.router-link-active {
    color: var(--color-cyan);
    background-color: var(--color-cyan-light);

    // Desktop only: underline indicator
    @media (min-width: 768px) {
      &::after {
        content: '';
        position: absolute;
        bottom: -1rem;
        left: 50%;
        transform: translateX(-50%);
        width: 60%;
        height: 2px;
        background-color: var(--color-cyan);
      }
    }
  }
}

.nav-text {
  margin: 1rem 0 0.5rem;
  padding: 0.75rem 1rem;
  font-weight: 500;
  font-size: var(--font-size-sm);
  color: var(--color-text-main);
  background-color: var(--color-background-alt);
  border-radius: var(--radius-md);
  text-align: center;
  
  @media (min-width: 768px) {
    margin: 0 0 0 var(--spacing-md);
    padding: 0.375rem 0.75rem;
  }
}
</style>
