<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import http from "@/utils/http-common";

const router = useRouter();

const email = ref("");
const name = ref("");
const message = ref("");
const isError = ref(false);
const isLoading = ref(false);

const findPassword = async () => {
    if (!email.value || !name.value) {
        message.value = "이메일과 이름을 모두 입력해주세요.";
        isError.value = true;
        return;
    }

    isLoading.value = true;
    message.value = "";
    isError.value = false;

    try {
        // API call to find password
        const response = await http.post("/auth/find-password", {
            email: email.value,
            name: name.value
        });

        message.value = response.data.message || "임시 비밀번호가 이메일로 전송되었습니다.";
        isError.value = false;
        
        // Optionally redirect to login after a delay
        setTimeout(() => {
            router.push({ name: "login" });
        }, 3000);

    } catch (error) {
        console.error(error);
        isError.value = true;
        if (error.response && error.response.data && error.response.data.message) {
            message.value = error.response.data.message;
        } else {
            message.value = "비밀번호 찾기에 실패했습니다. 정보를 확인해주세요.";
        }
    } finally {
        isLoading.value = false;
    }
};

const goBack = () => {
    router.go(-1);
};
</script>

<template>
    <div class="find-password-container">
        <div class="find-password-box">
            <h2>비밀번호 찾기</h2>
            <p class="description">가입시 등록한 이메일과 이름을 입력하시면<br>임시 비밀번호를 이메일로 보내드립니다.</p>
            
            <form @submit.prevent="findPassword">
                <div class="input-group">
                    <label for="email">이메일</label>
                    <input type="email" id="email" v-model="email" placeholder="example@ssafy.com" required />
                </div>
                
                <div class="input-group">
                    <label for="name">이름 (닉네임)</label>
                    <input type="text" id="name" v-model="name" placeholder="닉네임" required />
                </div>

                <div v-if="message" :class="['message', isError ? 'error' : 'success']">
                    {{ message }}
                </div>

                <div class="button-group">
                    <button type="submit" :disabled="isLoading" class="submit-btn">
                        {{ isLoading ? '전송 중...' : '임시 비밀번호 발송' }}
                    </button>
                    <button type="button" @click="goBack" class="cancel-btn">취소</button>
                </div>
            </form>
        </div>
    </div>
</template>

<style scoped>
.find-password-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 80vh;
    background-color: #f5f7fa;
}

.find-password-box {
    background: white;
    padding: 2rem;
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 400px;
    text-align: center;
}

h2 {
    margin-bottom: 1rem;
    color: #333;
}

.description {
    color: #666;
    margin-bottom: 2rem;
    font-size: 0.9rem;
    line-height: 1.5;
}

.input-group {
    margin-bottom: 1.5rem;
    text-align: left;
}

label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
    color: #444;
}

input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    font-size: 1rem;
    transition: border-color 0.2s;
}

input:focus {
    border-color: #4a90e2;
    outline: none;
}

.message {
    padding: 0.75rem;
    border-radius: 6px;
    margin-bottom: 1.5rem;
    font-size: 0.9rem;
}

.error {
    background-color: #fee2e2;
    color: #dc2626;
}

.success {
    background-color: #dcfce7;
    color: #16a34a;
}

.button-group {
    display: flex;
    gap: 1rem;
    flex-direction: column;
}

.submit-btn {
    background-color: #4a90e2;
    color: white;
    border: none;
    padding: 0.75rem;
    border-radius: 6px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: background-color 0.2s;
}

.submit-btn:hover:not(:disabled) {
    background-color: #357abd;
}

.submit-btn:disabled {
    background-color: #a0c4e8;
    cursor: not-allowed;
}

.cancel-btn {
    background-color: transparent;
    color: #666;
    border: 1px solid #ddd;
    padding: 0.75rem;
    border-radius: 6px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.2s;
}

.cancel-btn:hover {
    background-color: #f1f1f1;
}
</style>
