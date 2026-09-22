<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePlanStore } from '@/stores/plan'
import RoutePath from '@/components/map/RoutePath.vue'
import draggable from 'vuedraggable'
import { statusToBackend } from '@/utils/statusMapper'
import { formatDate } from '@/utils/dateFormatter'

const route = useRoute()
const router = useRouter()
const planStore = usePlanStore()

const plan = ref(null)
const loading = ref(true)
const map = ref(null)
const markers = ref([])
const polylines = ref([])

// Route related states
const priority = ref('RECOMMEND') // "RECOMMEND" | "TIME" | "DISTANCE"
const routeDuration = ref(0)
const routeDistance = ref(0)

// Drag and drop states
const isDragging = ref(false)
const localPlanItems = ref([])

// Watch plan.planItems and sync to localPlanItems
watch(() => plan.value?.planItems, (newItems) => {
  if (newItems) {
    localPlanItems.value = [...newItems]
  }
}, { immediate: true, deep: true })

const routeStart = computed(() => {
  if (!plan.value) return null

  // 1. Try plan.startLocation if it has coordinates "lat,lng"
  if (plan.value.startLocation && plan.value.startLocation.includes(',')) {
    try {
      const parts = plan.value.startLocation.split(',')
      if (parts.length === 2) {
        return {
          lat: parseFloat(parts[0].trim()),
          lng: parseFloat(parts[1].trim()),
          name: '출발지',
        }
      }
    } catch (e) {
      console.warn('Invalid startLocation format', e)
    }
  }

  // 2. Fallback to first plan item (use localPlanItems for real-time updates)
  if (localPlanItems.value && localPlanItems.value.length > 0) {
    const item = localPlanItems.value[0]
    return {
      lat: item.latitude,
      lng: item.longitude,
      name: item.title,
    }
  }
  return null
})

const routeDest = computed(() => {
  if (!localPlanItems.value || localPlanItems.value.length === 0) return null
  // Last item is destination
  // (unless we have a separate logic where start is item[0] and dest is item[N].
  // The user scenario usually implies Plan Items are the places to visit)
  // If we used item[0] as start, do we use item[last] as dest? Yes.
  // If we used startLocation as start, do we use item[last] as dest? Yes.

  const item = localPlanItems.value[localPlanItems.value.length - 1]

  // If start is the same as destination (e.g. only 1 item), route might be weird but valid (0 distance)
  return {
    lat: item.latitude,
    lng: item.longitude,
    name: item.title,
  }
})

const routeWaypoints = computed(() => {
  if (!localPlanItems.value || localPlanItems.value.length < 2) return []

  // Items between first and last
  // If startLocation was used as Origin, maybe we should visit ALL items?
  // Let's assume:
  // Origin: plan.startLocation OR item[0]
  // Dest: item[last]
  // Waypoints: item[0...last-1] (if StartLoc used) OR item[1...last-1] (if Item[0] used)

  let items = localPlanItems.value

  // If we used startLocation as Origin, we treat ALL items as waypoints except the last one?
  // Actually, usually "Route" connects A -> B -> C.
  // A=Start, C=End, B=Waypoint.
  // If StartLocation is given, A=StartLocation, B,C... = Items.
  // So Dest = Item[Last]. Waypoints = Item[0...Last-1].

  // If StartLocation NOT given, A=Item[0]. Dest=Item[Last]. Waypoints=Item[1...Last-1].

  const isStartLocUsed = plan.value?.startLocation && plan.value.startLocation.includes(',')

  let waypointsList = []

  if (isStartLocUsed) {
    // Waypoints are all items except the last one (which is destination)
    waypointsList = items.slice(0, items.length - 1)
  } else {
    // Waypoints are items between 0 and last
    if (items.length <= 2) return []
    waypointsList = items.slice(1, items.length - 1)
  }

  return waypointsList.map((item) => ({
    lat: item.latitude,
    lng: item.longitude,
    name: item.title,
  }))
})

const handleRouteInfoUpdate = (info) => {
  routeDuration.value = info.duration
  routeDistance.value = info.distance
}

const planId = computed(() => route.params.planId)

onMounted(async () => {
  await fetchPlanDetail()
  // index.html에서 SDK를 이미 로드한다고 가정
  loadKakaoMap()
})

/**
 * ✅ Kakao SDK가 index.html에서 로드되었는지 확인하고 초기화
 */
const loadKakaoMap = () => {
  console.log('Checking Kakao SDK from window...')

  if (!window.kakao || !window.kakao.maps) {
    console.error('❌ Kakao Maps SDK is not loaded. Check index.html script tag.')
    return
  }

  window.kakao.maps.load(() => {
    console.log('✅ Kakao Maps SDK loaded successfully')
    initializeMap()
  })
}

const fetchPlanDetail = async () => {
  loading.value = true
  try {
    // Fetch plan with items
    const response = await planStore.getPlanById(planId.value)
    plan.value = response.plan
    console.log('Plan detail loaded:', plan.value)
  } catch (error) {
    console.error('Failed to fetch plan detail:', error)
    alert('일정을 불러오는데 실패했습니다.')
    router.push('/mypage')
  } finally {
    loading.value = false
  }
}

const initializeMap = () => {
  console.log('Starting map initialization...')

  // Get map container
  const container = document.getElementById('map')
  if (!container) {
    console.error('Map container not found')
    return
  }

  console.log('Map container found:', container)

  // 기본 중심: 서울 시청
  let centerLat = 37.5665
  let centerLon = 126.978

  // plan.startLocation이 "lat, lon" 형태라면 파싱
  if (plan.value?.startLocation) {
    try {
      const [latStr, lonStr] = plan.value.startLocation.split(',').map((s) => s.trim())
      const lat = parseFloat(latStr)
      const lon = parseFloat(lonStr)
      if (!Number.isNaN(lat) && !Number.isNaN(lon)) {
        centerLat = lat
        centerLon = lon
      }
    } catch (e) {
      console.warn('Failed to parse startLocation, using default center.', e)
    }
  }

  const options = {
    center: new window.kakao.maps.LatLng(centerLat, centerLon),
    level: 3,
  }

  console.log('Creating map with options:', options)

  try {
    map.value = new window.kakao.maps.Map(container, options)
    console.log('✅ Map created successfully!')
  } catch (error) {
    console.error('❌ Error creating map:', error)
  }
}

const goBack = () => {
  router.push('/mypage')
}

// Drag and drop handlers
const onDragStart = () => {
  isDragging.value = true
}

const onDragEnd = async () => {
  isDragging.value = false

  // Update sequenceOrder based on new positions
  const updatedItems = localPlanItems.value.map((item, index) => ({
    ...item,
    sequenceOrder: index + 1
  }))

  // Save to server
  try {
    await planStore.updatePlanItemsOrder(plan.value.planId, updatedItems)
    console.log('Order updated successfully')
  } catch (error) {
    console.error('Failed to update order:', error)
    alert('순서 변경에 실패했습니다.')
    // Rollback on failure
    await fetchPlanDetail()
  }
}

const changeToggle = async () => {
  // 1. 이전 상태 저장 (롤백용)
  const previousStatus = plan.value.status;

  // 2. UI 먼저 업데이트 (낙관적) - 한글 상태로 토글
  if(plan.value.status === "진행중") {
    plan.value.status = "완료";
  } else {
    plan.value.status = "진행중"
  }

  // 3. API 호출 - 한글을 영어로 변환하여 전달
  try {
    const backendStatus = statusToBackend(plan.value.status);
    await planStore.updatePlanStatus(plan.value.planId, backendStatus);
    // 성공: 그대로 유지 (이미 UI 업데이트됨)
  } catch (error) {
    // 4. 실패: 롤백 + 에러 메시지
    plan.value.status = previousStatus;
    alert('상태 변경에 실패했습니다. 다시 시도해주세요.');
    console.error('Status toggle failed:', error);
  }
}
</script>

<template>
  <div class="plan-detail-container">
    <div v-if="loading" class="loading">일정을 불러오는 중...</div>

    <div v-else-if="plan" class="plan-detail">
      <!-- Header -->
      <div class="header">
        <button @click="goBack" class="btn-back">← 돌아가기</button>
        <h1>여행 일정 #{{ plan.planId }}</h1>
        <div class="header-info">
          <span class="status" :class="plan.status" @click="changeToggle">{{ plan.status }}</span>
          <span>📅 {{ formatDate(plan.createdAt) }}</span>
          <span>⏱️ {{ plan.totalTimeInput }}분</span>
        </div>
      </div>

      <!-- Main Content -->
      <div class="content">
        <!-- Left: Plan Items Dashboard -->
        <div class="plan-items-panel">
          <h2>일정 목록</h2>
          <div class="plan-meta">
            <div class="meta-item">
              <span class="label">출발지</span>
              <span class="value">{{ plan.startLocation || '-' }}</span>
            </div>
            <div class="meta-item">
              <span class="label">총 소요시간</span>
              <span class="value">{{ plan.totalTimeInput }}분</span>
            </div>
            <div class="meta-item">
              <span class="label">장소 수</span>
              <span class="value">{{ plan.planItems?.length || 0 }}개</span>
            </div>
          </div>

          <draggable
            v-if="plan.planItems && plan.planItems.length > 0"
            v-model="localPlanItems"
            :animation="200"
            handle=".drag-handle"
            ghost-class="ghost"
            class="items-list"
            @start="onDragStart"
            @end="onDragEnd"
            item-key="planItemId"
          >
            <template #item="{ element: item, index }">
              <div class="item-card" :class="{ 'is-dragging': isDragging }">
                <div class="drag-handle">⋮⋮</div>
                <div class="item-number">{{ index + 1 }}</div>
                <div class="item-content">
                  <h3 class="item-title">{{ item.title || `장소 #${item.placeId}` }}</h3>
                  <p v-if="item.overview" class="item-description">{{ item.overview }}</p>
                  <div class="item-meta">
                    <span class="meta-badge">🚌 {{ item.transportMode }}</span>
                    <span class="meta-badge">⏱️ {{ item.estimatedDuration }}분</span>
                    <span v-if="item.latitude && item.longitude" class="meta-badge">
                      📍 {{ item.latitude.toFixed(4) }}, {{ item.longitude.toFixed(4) }}
                    </span>
                  </div>
                </div>
              </div>
            </template>
          </draggable>
          <div v-else class="empty-state">일정에 장소가 없습니다.</div>
        </div>

        <!-- Right: Kakao Map -->
        <div class="map-panel">
          <div id="map" class="map-container"></div>
          <div class="map-legend">
            <div class="legend-item">
              <div class="legend-marker"></div>
              <span>방문 순서</span>
            </div>
            <div class="legend-item">
              <div class="legend-line"></div>
              <span>이동 경로</span>
            </div>
          </div>

          <!-- Route Controls -->
          <div class="route-controls">
            <select v-model="priority" class="priority-select">
              <option value="RECOMMEND">추천 경로</option>
              <option value="TIME">최단 시간</option>
              <option value="DISTANCE">최단 거리</option>
            </select>
            <div class="route-info" v-if="routeDistance > 0">
              <span>총 거리: {{ (routeDistance / 1000).toFixed(1) }}km</span>
              <span>예상 시간: {{ Math.round(routeDuration / 60) }}분</span>
            </div>
          </div>

          <!-- Route Path Component -->
          <RoutePath
            v-if="map && routeStart && routeDest"
            :map="map"
            :start="routeStart"
            :destination="routeDest"
            :waypoints="routeWaypoints"
            :priority="priority"
            @update-route-info="handleRouteInfoUpdate"
          />
        </div>
      </div>
    </div>

    <div v-else class="error">일정을 찾을 수 없습니다.</div>
  </div>
</template>

<style scoped>
.plan-detail-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0;
}

.loading,
.error {
  text-align: center;
  padding: 4rem;
  color: white;
  font-size: 1.2rem;
}

.plan-detail {
  max-width: 100%;
  margin: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 0;
  margin-bottom: 0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.btn-back {
  background: transparent;
  border: none;
  color: #667eea;
  font-size: 1rem;
  cursor: pointer;
  padding: 0.5rem 1rem;
  margin-bottom: 1rem;
  transition: all 0.3s;
}

.btn-back:hover {
  background: #f0f0f0;
  border-radius: 6px;
}

.header h1 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 2rem;
}

.header-info {
  display: flex;
  gap: 1.5rem;
  align-items: center;
  color: #666;
}

.status {
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  background: #e3f2fd;
  color: #1976d2;
}

.status:hover {
  background-color: #667eea;
  color: white;
}

.content {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 0;
  flex: 1;
  overflow: hidden;
}

/* Left Panel - Plan Items */
.plan-items-panel {
  background: white;
  border-radius: 0;
  padding: 2rem;
  overflow-y: auto;
  box-shadow: none;
  height: 100%;
}

.plan-items-panel h2 {
  margin: 0 0 1.5rem 0;
  color: #333;
  font-size: 1.5rem;
}

.plan-meta {
  background: #f8f9fa;
  padding: 1rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem 0;
  border-bottom: 1px solid #e0e0e0;
}

.meta-item:last-child {
  border-bottom: none;
}

.meta-item .label {
  font-weight: 600;
  color: #666;
}

.meta-item .value {
  color: #333;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.item-card {
  display: flex;
  gap: 1rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid #4caf50;
  transition: all 0.3s;
}

.item-card:hover {
  background: #e8f5e9;
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.2);
}

.drag-handle {
  width: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  cursor: grab;
  font-size: 1.2rem;
  user-select: none;
  flex-shrink: 0;
  transition: color 0.2s;
}

.drag-handle:active {
  cursor: grabbing;
}

.item-card:hover .drag-handle {
  color: #4caf50;
}

.item-card.is-dragging {
  opacity: 0.5;
}

.ghost {
  opacity: 0.3;
  background: #e8f5e9;
  border: 2px dashed #4caf50;
}

.items-list.sortable-drag .item-card:hover {
  transform: none;
  background: #f8f9fa;
}

.item-number {
  width: 32px;
  height: 32px;
  background: #4caf50;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  flex-shrink: 0;
}

.item-content {
  flex: 1;
}

.item-title {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 1.1rem;
}

.item-description {
  margin: 0 0 0.75rem 0;
  color: #666;
  font-size: 0.9rem;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-clamp: 2;
}

.item-meta {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.meta-badge {
  padding: 0.25rem 0.75rem;
  background: white;
  border-radius: 12px;
  font-size: 0.85rem;
  color: #666;
  border: 1px solid #e0e0e0;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: #999;
}

/* Right Panel - Map */
.map-panel {
  background: white;
  border-radius: 0;
  padding: 0;
  box-shadow: none;
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.map-container {
  width: 100%;
  flex: 1;
  border-radius: 0;
  overflow: hidden;
}

.map-legend {
  display: flex;
  gap: 2rem;
  padding: 1rem 2rem;
  background: #f8f9fa;
  border-radius: 0;
  margin-top: 0;
  flex-shrink: 0;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #666;
}

.legend-marker {
  width: 24px;
  height: 24px;
  background: white;
  border: 2px solid #4caf50;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.legend-line {
  width: 40px;
  height: 4px;
  background: #4caf50;
  border-radius: 2px;
}

.route-controls {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 2;
  background: white;
  padding: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.priority-select {
  padding: 6px;
  border-radius: 4px;
  border: 1px solid #ddd;
}

.route-info {
  font-size: 0.9rem;
  font-weight: bold;
  color: #333;
  display: flex;
  flex-direction: column;
}

/* Responsive */
@media (max-width: 1200px) {
  .content {
    grid-template-columns: 1fr;
    height: auto;
  }

  .plan-items-panel {
    max-height: 400px;
  }

  .map-panel {
    height: 500px;
  }

  .map-container {
    height: calc(100% - 60px);
  }
}
</style>
