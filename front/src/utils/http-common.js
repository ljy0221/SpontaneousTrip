import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'https://dongyeop.shop/final',
  //baseURL: 'http://localhost:9001/final',
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
  withCredentials: true,
})

// Add a request interceptor
http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken') || sessionStorage.getItem('accessToken')
    if (token) {
      config.headers['access'] = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Add a response interceptor for automatic token refresh
let isRefreshing = false
let failedQueue = []

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })
  failedQueue = []
}

http.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    // Don't retry if this is already a refresh request or if skipAuthRefresh is set
    if (originalRequest.url?.includes('/auth/refresh') || originalRequest.skipAuthRefresh) {
      return Promise.reject(error)
    }

    // If error is 401 and we haven't tried to refresh yet
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        // If already refreshing, queue this request
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject })
        }).then(token => {
          originalRequest.headers['access'] = token
          return http(originalRequest)
        }).catch(err => {
          return Promise.reject(err)
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        // Call refresh token endpoint with skipAuthRefresh flag
        const response = await http.post('/auth/refresh', {}, {
          skipAuthRefresh: true
        })
        const { accessToken } = response.data

        // Update token in storage
        if (localStorage.getItem('userId')) {
          localStorage.setItem('accessToken', accessToken)
        } else if (sessionStorage.getItem('userId')) {
          sessionStorage.setItem('accessToken', accessToken)
        }

        // Update the failed request with new token
        originalRequest.headers['access'] = accessToken

        // Process queued requests
        processQueue(null, accessToken)

        isRefreshing = false

        // Retry the original request
        return http(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError, null)
        isRefreshing = false

        // If refresh fails, clear storage and redirect to login
        localStorage.removeItem('userId')
        localStorage.removeItem('accessToken')
        localStorage.removeItem('nickname')
        sessionStorage.removeItem('userId')
        sessionStorage.removeItem('accessToken')
        sessionStorage.removeItem('nickname')

        // Redirect to login page
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }

        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default http
