import { ref } from 'vue'
import { defineStore } from 'pinia'
import http from '@/utils/http-common'

export const useBoardStore = defineStore('board', () => {
  const posts = ref([])
  const currentPost = ref(null)
  const pageInfo = ref({
    currentPage: 0,
    pageSize: 10,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false
  })

  // 공지사항 목록 조회
  const getPosts = async (condition) => {
    try {
      const response = await http.get('/notice', { params: condition })
      posts.value = response.data.data
      pageInfo.value = response.data.pageInfo
    } catch (error) {
      console.error('Error fetching posts:', error)
    }
  }

  // 공지사항 상세 조회
  const getPostById = async (id) => {
    try {
      const response = await http.get(`/notice/${id}`)
      currentPost.value = response.data.notice
    } catch (error) {
      console.error('Error fetching post:', error)
    }
  }

  // 공지사항 작성
  const createPost = async (post) => {
    try {
      await http.post('/notice', post)
      await getPosts() // 목록 갱신
    } catch (error) {
      console.error('Error creating post:', error)
      throw error
    }
  }

  // 공지사항 수정
  const updatePost = async (id, post) => {
    try {
      await http.put(`/notice/${id}`, post)
      await getPostById(id) // 상세 정보 갱신
    } catch (error) {
      console.error('Error updating post:', error)
      throw error
    }
  }

  // 공지사항 삭제
  const deletePost = async (id) => {
    try {
      await http.delete(`/notice/${id}`)
      await getPosts() // 목록 갱신
    } catch (error) {
      console.error('Error deleting post:', error)
      throw error
    }
  }

  return { posts, currentPost, pageInfo, getPosts, getPostById, createPost, updatePost, deletePost }
})
