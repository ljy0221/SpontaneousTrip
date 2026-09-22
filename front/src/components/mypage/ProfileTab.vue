<script setup>
import { ref, watch } from 'vue'
import { formatDate } from '@/utils/dateFormatter'

const props = defineProps({
  user: {
    type: Object,
    default: null
  },
  authStore: {
    type: Object,
    required: true
  },
  userStore: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['refresh-user'])

// UI States
const profileMode = ref('view') // 'view' | 'edit' | 'password' | 'delete'

// Forms
const editForm = ref({
  nickname: ''
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  newPasswordConfirm: ''
})

const deleteForm = ref({
  password: ''
})

// Watch user prop to update form
watch(() => props.user, (newUser) => {
  if (newUser) {
    editForm.value.nickname = newUser.nickname
  }
}, { immediate: true })

const handleLogout = () => {
  props.authStore.logout()
}

// Edit Profile Handlers
const startEditProfile = () => {
  profileMode.value = 'edit'
  editForm.value.nickname = props.user.nickname
}

const cancelEditProfile = () => {
  profileMode.value = 'view'
}

const saveProfile = async () => {
  try {
    await props.userStore.updateUserInfo({
      nickname: editForm.value.nickname
    })
    alert('프로필이 성공적으로 업데이트되었습니다!')
    profileMode.value = 'view'
    emit('refresh-user')
  } catch (error) {
    console.error('Failed to update profile:', error)
    alert('프로필 업데이트에 실패했습니다.')
  }
}

// Change Password Handlers
const startChangePassword = () => {
  profileMode.value = 'password'
  passwordForm.value = {
    currentPassword: '',
    newPassword: '',
    newPasswordConfirm: ''
  }
}

const cancelChangePassword = () => {
  profileMode.value = 'view'
}

const savePassword = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.newPasswordConfirm) {
    alert('새 비밀번호가 일치하지 않습니다!')
    return
  }

  try {
    await props.userStore.updateUserInfo({
      currentPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })
    alert('비밀번호가 성공적으로 변경되었습니다!')
    profileMode.value = 'view'
  } catch (error) {
    console.error('Failed to change password:', error)
    alert('비밀번호 변경에 실패했습니다. 현재 비밀번호를 확인해주세요.')
  }
}

// Delete Account Handlers
const startDeleteAccount = () => {
  profileMode.value = 'delete'
  deleteForm.value.password = ''
}

const cancelDeleteAccount = () => {
  profileMode.value = 'view'
}

const confirmDeleteAccount = async () => {
  if (!deleteForm.value.password) {
    alert('계정 삭제를 확인하려면 비밀번호를 입력해주세요.')
    return
  }

  if (!confirm('정말로 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
    return
  }

  try {
    await props.userStore.deleteAccount(deleteForm.value.password)
    alert('계정이 삭제되었습니다.')
    props.authStore.logout()
  } catch (error) {
    console.error('Failed to delete account:', error)
    alert('계정 삭제에 실패했습니다. 비밀번호를 확인해주세요.')
  }
}

</script>

<template>
  <div class="card profile-card" v-if="user">
    <div class="profile-header">
      <div class="avatar">
        {{ user.nickname ? user.nickname.charAt(0) : 'U' }}
      </div>
      <h2>{{ user.nickname }}</h2>
      <p class="email">{{ user.email }}</p>
    </div>

    <!-- Normal View -->
    <div v-if="profileMode === 'view'">
      <div class="profile-details">
        <div class="detail-item">
          <label>이메일</label>
          <span>{{ user.email }}</span>
        </div>
        <div class="detail-item">
          <label>닉네임</label>
          <span>{{ user.nickname }}</span>
        </div>
        <div class="detail-item">
          <label>가입일</label>
          <span>{{ formatDate(user.joinDate) }}</span>
        </div>
        <div class="detail-item">
          <label>마지막 로그인</label>
          <span>{{ formatDate(user.lastLogin) }}</span>
        </div>
      </div>

      <div class="actions">
        <button @click="startEditProfile" class="btn btn-secondary">프로필 수정</button>
        <button @click="startChangePassword" class="btn btn-secondary">비밀번호 변경</button>
        <button @click="handleLogout" class="btn btn-danger">로그아웃</button>
        <button @click="startDeleteAccount" class="btn btn-danger-outline">계정 삭제</button>
      </div>
    </div>

    <!-- Edit Profile View -->
    <div v-else-if="profileMode === 'edit'">
      <div class="form-group">
        <label class="form-label">닉네임</label>
        <input
          v-model="editForm.nickname"
          type="text"
          class="form-input"
          placeholder="새 닉네임 입력"
        />
      </div>
      <div class="actions">
        <button @click="saveProfile" class="btn btn-primary">저장</button>
        <button @click="cancelEditProfile" class="btn btn-secondary">취소</button>
      </div>
    </div>

    <!-- Change Password View -->
    <div v-else-if="profileMode === 'password'">
      <div class="form-group">
        <label class="form-label">현재 비밀번호</label>
        <input
          v-model="passwordForm.currentPassword"
          type="password"
          class="form-input"
          placeholder="현재 비밀번호 입력"
        />
      </div>
      <div class="form-group">
        <label class="form-label">새 비밀번호</label>
        <input
          v-model="passwordForm.newPassword"
          type="password"
          class="form-input"
          placeholder="새 비밀번호 입력"
        />
      </div>
      <div class="form-group">
        <label class="form-label">새 비밀번호 확인</label>
        <input
          v-model="passwordForm.newPasswordConfirm"
          type="password"
          class="form-input"
          placeholder="새 비밀번호 확인"
        />
      </div>
      <div class="actions">
        <button @click="savePassword" class="btn btn-primary">비밀번호 변경</button>
        <button @click="cancelChangePassword" class="btn btn-secondary">취소</button>
      </div>
    </div>

    <!-- Delete Account View -->
    <div v-else-if="profileMode === 'delete'">
      <div class="warning-box">
        <h3>⚠️ 경고</h3>
        <p>이 작업은 되돌릴 수 없습니다. 모든 데이터가 영구적으로 삭제됩니다.</p>
      </div>
      <div class="form-group">
        <label class="form-label">비밀번호를 입력하여 확인</label>
        <input
          v-model="deleteForm.password"
          type="password"
          class="form-input"
          placeholder="비밀번호 입력"
        />
      </div>
      <div class="actions">
        <button @click="confirmDeleteAccount" class="btn btn-danger">삭제 확인</button>
        <button @click="cancelDeleteAccount" class="btn btn-secondary">취소</button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-card {
	max-width: 700px;
	margin: 0 auto;
	text-align: center;
	box-shadow: var(--shadow-xl);
}

.profile-header {
	margin-bottom: 2.5rem;
	padding-bottom: 2rem;
	border-bottom: 2px solid var(--color-border-light);

	h2 {
		font-size: var(--font-size-2xl);
		font-weight: 700;
		color: var(--color-text-main);
		margin: 0.75rem 0 0.5rem;
	}
}

.avatar {
	width: 120px;
	height: 120px;
	background: linear-gradient(135deg, var(--color-cyan) 0%, var(--color-teal) 100%);
	color: white;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 3rem;
	font-weight: 700;
	margin: 0 auto 1rem;
	box-shadow: var(--shadow-lg);
	border: 4px solid var(--color-surface);
}

.email {
	color: var(--color-text-muted);
	font-size: var(--font-size-base);
	font-weight: 500;
}

.profile-details {
	text-align: left;
	margin-bottom: 2rem;
	background-color: var(--color-background-alt);
	border-radius: var(--radius-lg);
	padding: 1.5rem;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0.875rem 0;
	border-bottom: 1px solid var(--color-border-light);

	&:last-child {
		border-bottom: none;
	}

	label {
		color: var(--color-text-muted);
		font-weight: 600;
		font-size: var(--font-size-sm);
		text-transform: uppercase;
		letter-spacing: 0.05em;
	}

	span {
		font-weight: 600;
		color: var(--color-text-main);
	}
}

.actions {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;

	.btn {
		width: 100%;
		padding: 0.875rem;
		font-weight: 600;
	}

	.btn-primary,
	.btn-secondary,
	.btn-danger {
		background-color: var(--color-cyan);

		&:hover {
			background-color: var(--color-cyan-hover);
		}
	}

	.btn-secondary {
		background-color: var(--color-surface);
		color: var(--color-text-main);
		border: 2px solid var(--color-border);

		&:hover {
			background-color: var(--color-background);
			border-color: var(--color-cyan);
			color: var(--color-cyan);
		}
	}

	.btn-danger {
		background-color: var(--color-danger);

		&:hover {
			background-color: #DC2626;
		}
	}
}

.form-group {
	text-align: left;
	margin-bottom: 1.5rem;
}

.warning-box {
	background-color: var(--color-danger-bg);
	border: 2px solid var(--color-danger-border);
	border-radius: var(--radius-lg);
	padding: 1.5rem;
	margin-bottom: 2rem;
	text-align: center;

	h3 {
		color: var(--color-danger);
		margin: 0 0 0.75rem 0;
		font-size: var(--font-size-xl);
		font-weight: 700;
	}

	p {
		color: var(--color-text-main);
		margin: 0;
		font-weight: 500;
		line-height: 1.6;
	}
}

.btn-danger-outline {
	background-color: transparent;
	color: var(--color-danger);
	border: 2px solid var(--color-danger);

	&:hover {
		background-color: var(--color-danger);
		color: white;
		transform: translateY(-1px);
		box-shadow: var(--shadow-md);
	}
}

@media (max-width: 768px) {
	.profile-card {
		padding: var(--spacing-lg);
	}

	.avatar {
		width: 100px;
		height: 100px;
		font-size: 2.5rem;
	}
}
</style>
