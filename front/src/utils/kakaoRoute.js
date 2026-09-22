// kakaoRoute.js

import http from '@/utils/http-common'

export async function fetchRoute(start, waypoints, destination, priority = 'RECOMMEND') {
  try {
    const body = {
      origin: {
        x: start.lng, // ✅ 경도
        y: start.lat, // ✅ 위도
        name: start.name || '출발지',
      },
      destination: {
        x: destination.lat, // ✅ 경도
        y: destination.lng, // ✅ 위도
        name: destination.name || '도착지',
      },
      waypoints: (waypoints || []).map((w) => ({
        x: w.lat, // ✅ 경도
        y: w.lng, // ✅ 위도
        name: w.name,
      })),
      priority,
    }

    console.log('🚗 /map/direction request body:', JSON.stringify(body, null, 2))

    const response = await http.post('/map/direction', body)
    const data = response.data

    if (!data || !data.routes || data.routes.length === 0) {
      console.error('No routes found.')
      return null
    }

    const route = data.routes[0]
    const summary = route.summary
    const sections = route.sections

    const path = sections.flatMap((section) =>
      section.roads.flatMap((road) => {
        const coords = []
        for (let i = 0; i < road.vertexes.length; i += 2) {
          coords.push(
            new window.kakao.maps.LatLng(
              road.vertexes[i + 1], // lat
              road.vertexes[i], // lng
            ),
          )
        }
        return coords
      }),
    )

    return {
      summary,
      path,
      duration: summary.duration,
      distance: summary.distance,
    }
  } catch (error) {
    console.error('Failed to fetch route:', error)
    return null
  }
}
