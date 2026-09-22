import './assets/main.scss'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

const app = createApp(App)

app.config.errorHandler = (err, instance, info) => {
  console.error('Global Error:', err)
  alert(`Error: ${err.message}\nCheck console for details.`)
}

app.use(createPinia())
app.use(router)

app.mount('#app')
