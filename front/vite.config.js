import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      '/final': {
        target: 'http://localhost:9001',
        changeOrigin: true,
        secure: false,
      },
      '/api': {
        target: 'http://3.37.88.46:9000',
        changeOrigin: true,
        secure: false,
      }
    }
  }
})
