<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { fetchRoute } from '@/utils/kakaoRoute'

const props = defineProps({
  map: Object,
  start: Object,
  destination: Object,
  waypoints: Array,
  priority: String,
})

const emit = defineEmits(['update-route-info'])

const polylineRef = ref(null)
const markersRef = ref([])

// ------------------------------------------
// 1) 숫자 마커 이미지 생성 함수
// ------------------------------------------
function createNumberMarkerImage(number) {
  const svg = `
    <svg width="38" height="48" xmlns="http://www.w3.org/2000/svg">
      <path d="M19 0C9 0 2 7 2 16c0 12 17 30 17 30s17-18 17-30C36 7 29 0 19 0z"
            fill="#4CAF50"/>
      <circle cx="19" cy="16" r="9" fill="white"/>
      <text x="19" y="20" font-size="12" font-weight="bold"
            fill="#4CAF50" text-anchor="middle">${number}</text>
    </svg>
  `

  return new window.kakao.maps.MarkerImage(
    'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg),
    new window.kakao.maps.Size(38, 48),
    { offset: new window.kakao.maps.Point(19, 48) },
  )
}

const clearMarkers = () => {
  markersRef.value.forEach((m) => m.setMap(null))
  markersRef.value = []
}

// ------------------------------------------
// 2) 마커 생성 (번호 + 인포윈도우)
// ------------------------------------------
const createMarker = (lat, lng, title, number) => {
  const kakao = window.kakao
  const pos = new kakao.maps.LatLng(lat, lng)

  const marker = new kakao.maps.Marker({
    position: pos,
    clickable: true,
    image: createNumberMarkerImage(number), // ← 번호 마커 적용
  })

  marker.setMap(props.map)

  // 인포윈도우
  const info = new kakao.maps.InfoWindow({
    content: `<div style="padding:5px; font-size:14px;">${title}</div>`,
    removable: true,
  })

  kakao.maps.event.addListener(marker, 'click', () => {
    info.open(props.map, marker)
  })

  markersRef.value.push(marker)
}

// ------------------------------------------
// 3) 전체 마커 렌더링
// ------------------------------------------
const drawMarkers = () => {
  if (!props.map) return

  clearMarkers()

  let number = 1

  // 출발지 = 1번
  createMarker(props.start.lat, props.start.lng, props.start.name || '출발지', number++)

  // 경유지 = 2번부터 자동 증가
  props.waypoints.forEach((w) => {
    createMarker(w.lng, w.lat, w.name || '경유지', number++)
  })

  // 목적지 = 마지막 번호
  createMarker(
    props.destination.lng,
    props.destination.lat,
    props.destination.name || '도착지',
    number++,
  )
}

// ------------------------------------------
// 4) 경로 라인 그리기
// ------------------------------------------
const drawRoute = async () => {
  if (!props.map) return

  if (polylineRef.value) {
    polylineRef.value.setMap(null)
    polylineRef.value = null
  }
  clearMarkers()

  drawMarkers()

  const result = await fetchRoute(props.start, props.waypoints, props.destination, props.priority)
  if (!result) return

  const kakao = window.kakao

  const polyline = new kakao.maps.Polyline({
    path: result.path,
    strokeWeight: 5,
    strokeColor: '#2979FF', // 초록색 라인
    strokeOpacity: 0.9,
    strokeStyle: 'solid',
  })

  polyline.setMap(props.map)
  polylineRef.value = polyline

  emit('update-route-info', {
    duration: result.duration,
    distance: result.distance,
  })

  const bounds = new kakao.maps.LatLngBounds()
  result.path.forEach((latlng) => bounds.extend(latlng))
  props.map.setBounds(bounds)
}

watch(() => [props.start, props.destination, props.waypoints, props.priority], drawRoute, {
  deep: true,
})

onMounted(drawRoute)
onUnmounted(clearMarkers)
</script>

<template>
  <div style="display: none"></div>
</template>
