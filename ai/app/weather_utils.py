
import math
import os
import httpx
import asyncio
from datetime import datetime, timedelta
from typing import Dict, Any, Optional

# ============================================================
# 1. 격자 변환 유틸리티 (GeoGridConverter 포팅)
# ============================================================

def convert_to_grid(lat: float, lon: float) -> Dict[str, int]:
    """
    위경도를 기상청 격자좌표(X, Y)로 변환
    """
    RE = 6371.00877  # 지구 반경(km)
    GRID = 5.0       # 격자 간격(km)
    SLAT1 = 30.0     # 표준위도1
    SLAT2 = 60.0     # 표준위도2
    OLON = 126.0     # 기준점 경도
    OLAT = 38.0      # 기준점 위도
    XO = 43          # 기준점 X좌표
    YO = 136         # 기준점 Y좌표

    DEGRAD = math.pi / 180.0
    
    re = RE / GRID
    slat1 = SLAT1 * DEGRAD
    slat2 = SLAT2 * DEGRAD
    olon = OLON * DEGRAD
    olat = OLAT * DEGRAD
    
    sn = math.tan(math.pi * 0.25 + slat2 * 0.5) / math.tan(math.pi * 0.25 + slat1 * 0.5)
    sn = math.log(math.cos(slat1) / math.cos(slat2)) / math.log(sn)
    sf = math.tan(math.pi * 0.25 + slat1 * 0.5)
    sf = math.pow(sf, sn) * math.cos(slat1) / sn
    ro = math.tan(math.pi * 0.25 + olat * 0.5)
    ro = re * sf / math.pow(ro, sn)
    
    ra = math.tan(math.pi * 0.25 + (lat) * DEGRAD * 0.5)
    ra = re * sf / math.pow(ra, sn)
    theta = lon * DEGRAD - olon
    
    if theta > math.pi:
        theta -= 2.0 * math.pi
    if theta < -math.pi:
        theta += 2.0 * math.pi
    theta *= sn
    
    nx = int(math.floor(ra * math.sin(theta) + XO + 0.5))
    ny = int(math.floor(ro - ra * math.cos(theta) + YO + 0.5))
    
    return {"nx": nx, "ny": ny}

# ============================================================
# 2. 날씨 API 호출 (WeatherApiService 포팅)
# ============================================================

async def get_current_weather(nx: int, ny: int) -> Dict[str, str]:
    """
    기상청 초단기실황 조회
    """
    service_key = os.getenv("WEATHER_API_SERVICEKEY", "")
    if not service_key:
        print("⚠️ WEATHER_API_SERVICE_KEY not found in env")
        return None

    # 시간 설정
    now = datetime.now()
    if now.minute < 30:
        now = now - timedelta(hours=1)
    
    base_date = now.strftime("%Y%m%d")
    base_time = now.strftime("%H") + "00"
    
    url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst"
    
    params = {
        "serviceKey": service_key,
        "pageNo": "1",
        "numOfRows": "10",
        "dataType": "JSON",
        "base_date": base_date,
        "base_time": base_time,
        "nx": nx,
        "ny": ny
    }
    
    # 디코딩된 키가 필요할 수 있으므로 httpx 사용 시 주의 (보통은 params에 넣으면 인코딩됨)
    # 공공데이터포털 키는 이미 인코딩된 경우와 아닌 경우가 섞여 있어서, 
    # requests/httpx가 이중 인코딩 하지 않도록 주의해야 함.
    # 여기서는 파라미터를 직접 구성하여 URL에 붙이는 방식을 사용하거나, 
    # serviceKey가 Decoding된 상태라면 params에 넣어도 됨.
    # 안전하게 URL 스트링을 직접 구성 (requests 라이브러리 이슈 방지)
    
    # params 제외하고 기본 URL 구성
    async with httpx.AsyncClient() as client:
        try:
            # serviceKey는 이미 인코딩되어 있을 수 있으므로 safe='' 설정 고려
            # 하지만 간단하게 params로 시도해보고, 안되면 URL 직접 구성 방식으로 변경
            # 일반적으로 Encoding Key를 그대로 보내야 함
            
            # 파라미터 수동 구성 (서비스키 이중 인코딩 방지)
            query_string = f"?serviceKey={service_key}&pageNo=1&numOfRows=10&dataType=JSON&base_date={base_date}&base_time={base_time}&nx={nx}&ny={ny}"
            full_url = url + query_string
            
            print(f"🌦️ Fetching weather: {base_date} {base_time} ({nx}, {ny})")
            
            response = await client.get(full_url, timeout=10.0)
            data = response.json()
            
            if data['response']['header']['resultCode'] != '00':
                print(f"❌ Weather API Error: {data['response']['header']['resultMsg']}")
                return None
            
            items = data['response']['body']['items']['item']
            
            weather_data = {
                "baseDate": base_date,
                "baseTime": base_time,
                "nx": str(nx),
                "ny": str(ny),
                "temperature": "-",
                "humidity": "-",
                "precipitation": "-",
                "windSpeed": "-",
                "windDirection": "-",
                "skyStatus": "-"
            }
            
            for item in items:
                category = item['category']
                value = item['obsrValue']
                
                if category == 'T1H': # 기온
                    weather_data['temperature'] = value
                elif category == 'REH': # 습도
                    weather_data['humidity'] = value
                elif category == 'RN1': # 1시간 강수량
                    weather_data['precipitation'] = value
                elif category == 'WSD': # 풍속
                    weather_data['windSpeed'] = value
                elif category == 'VEC': # 풍향
                    weather_data['windDirection'] = value
                elif category == 'PTY': # 강수형태
                    weather_data['skyStatus'] = value
                    
            return weather_data
            
        except Exception as e:
            print(f"❌ Failed to fetch weather: {e}")
            return None
