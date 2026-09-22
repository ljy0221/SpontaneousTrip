from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from typing import Optional, Generator
import os
import httpx
import traceback
from dotenv import load_dotenv

# DB 관련 라이브러리
from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker, Session

# 벡터 DB 및 AI 관련
import chromadb
from chromadb.errors import NotFoundError
from openai import AsyncOpenAI
from sentence_transformers import SentenceTransformer
from fastapi.responses import StreamingResponse
from weather_utils import convert_to_grid, get_current_weather

load_dotenv()

app = FastAPI(
    title="SSAFY Pass RAG Chatbot API",
    description="SQLAlchemy 커넥션 풀이 적용된 RAG 챗봇",
    version="1.1.0"
)

# CORS 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- 1. SQLAlchemy 설정 (커넥션 풀) ---
DB_USER = os.getenv("DB_USER", "root")
DB_PASSWORD = os.getenv("DB_PASSWORD", "")
DB_HOST = os.getenv("DB_HOST", "")
DB_NAME = os.getenv("DB_NAME", "")

# pool_size: 기본 연결 수, max_overflow: 필요 시 추가 연결 수, pool_recycle: 연결 유지 시간
DATABASE_URL = f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}/{DB_NAME}?charset=utf8mb4"

engine = create_engine(
    DATABASE_URL,
    pool_size=10,
    max_overflow=20,
    pool_recycle=3600,
    pool_pre_ping=True  # 쿼리 실행 전 연결 상태를 자동으로 확인 (ping 기능)
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Dependency: 요청마다 DB 세션을 생성하고 완료 후 닫음
def get_db() -> Generator:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# --- 2. 환경 변수 및 전역 변수 ---
GMS_API_BASE = os.getenv("GMS_API_BASE")
GMS_API_KEY = os.getenv("GMS_API_KEY") or os.getenv("OPENAI_API_KEY")

collection = None
openai_client = None
embedding_model = None

# OpenAI 클라이언트 초기화
def get_gms_client():
    return AsyncOpenAI(
        api_key=GMS_API_KEY,
        base_url=GMS_API_BASE,
        http_client=httpx.AsyncClient(timeout=600.0, verify=False)
    )

def get_embedding(text):
    return embedding_model.encode(text.replace("\n", " ")).tolist()

# --- 3. Pydantic 모델 ---
class ChatRequest(BaseModel):
    query: str
    place_id: Optional[int] = None
    max_tokens: int = 500
    temperature: float = 0.5

class SummaryRequest(BaseModel):
    place_id: int
    max_length: int = 550

class WeatherRequest(BaseModel):
    lat: float
    lon: float

class IntroRequest(BaseModel):
    place_id: int
    lat: Optional[float] = None
    lon: Optional[float] = None
    place_name: Optional[str] = None
    overview: Optional[str] = None


# --- 4. 초기화 이벤트 ---
@app.on_event("startup")
async def startup():
    global collection, openai_client, embedding_model
    print("🚀 서비스 초기화 중 (SQLAlchemy 모드)...")
    
    embedding_model = SentenceTransformer('jhgan/ko-sroberta-multitask')
    openai_client = get_gms_client()
    
    # ChromaDB 설정
    DB_PATH = os.getenv("CHROMA_DB_PATH", "../data/vectordb")
    chroma_client = chromadb.PersistentClient(path=DB_PATH)
    try:
        collection = chroma_client.get_collection(name="ssafy_places")
    except (NotFoundError, ValueError):
        collection = chroma_client.create_collection(name="ssafy_places")
    
    print("✅ 초기화 완료!")

# --- 5. 헬퍼 함수 (DB 세션 사용) ---
def search_similar(query_text, top_k=5, filter_dict=None):
    query_embedding = get_embedding(query_text)
    return collection.query(query_embeddings=[query_embedding], n_results=top_k, where=filter_dict)

def get_place_info(db: Session, place_id: int):
    query = text("SELECT * FROM hotplace WHERE place_id = :place_id")
    result = db.execute(query, {"place_id": place_id}).mappings().fetchone()
    return result

def get_reviews(db: Session, place_id: int, limit=3):
    query = text("""
        SELECT r.*, u.nickname FROM review r
        JOIN users u ON r.user_id = u.user_id
        WHERE r.place_id = :place_id
        ORDER BY r.rating DESC, r.created_at DESC LIMIT :limit
    """)
    result = db.execute(query, {"place_id": place_id, "limit": limit}).mappings().fetchall()
    return result

# --- 6. API 엔드포인트 ---

@app.post("/api/chat")
async def chat(request: ChatRequest, db: Session = Depends(get_db)):
    try:
        filter_dict = {"place_id": request.place_id} if request.place_id else None
        search_results = search_similar(request.query, top_k=5, filter_dict=filter_dict)
        context_docs = "\n".join([f"- {doc}" for doc in search_results['documents'][0][:3]])
        
        place_info_text = ""
        weather_info_text = ""
        
        if request.place_id:
            place = get_place_info(db, request.place_id)
            if place:
                place_info_text = f"[장소] {place['place_name']}\n주소: {place['address']}\n설명: {place.get('overview','')}"
                
                # 날씨 정보 추가
                try:
                    if place.get('mapy') and place.get('mapx'):
                        grid = convert_to_grid(float(place['mapy']), float(place['mapx']))
                        weather_data = await get_current_weather(grid['nx'], grid['ny'])
                        if weather_data:
                            temp = weather_data.get('temperature', '-')
                            sky_code = weather_data.get('skyStatus', '0')
                            pty_map = {'0': '맑음', '1': '비', '2': '비/눈', '3': '눈', '5': '빗방울', '6': '진눈깨비', '7': '눈날림'}
                            sky = pty_map.get(sky_code, '알수없음')
                            weather_info_text = f"\n현재 날씨: 기온 {temp}°C, 상태 {sky}"
                except Exception as w_e:
                    print(f"Weather fetch failed: {w_e}")

        prompt = f"{place_info_text}{weather_info_text}\n\n[관련 정보]\n{context_docs}\n\n질문: {request.query}\n답변:"

        async def generate():
            stream = await openai_client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[{"role": "user", "content": prompt}],
                max_tokens=request.max_tokens,
                temperature=request.temperature,
                stream=True
            )
            async for chunk in stream:
                if chunk.choices and chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content

        return StreamingResponse(generate(), media_type="text/event-stream")
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/summary")
async def generate_summary(request: SummaryRequest, db: Session = Depends(get_db)):
    try:
        place = get_place_info(db, request.place_id)
        if not place:
            async def empty_gen(): yield "장소 정보를 찾을 수 없습니다."
            return StreamingResponse(empty_gen(), media_type="text/plain")

        reviews = get_reviews(db, request.place_id, limit=3)
        reviews_text = "\n".join([f"- {r['content'][:50]}" for r in reviews])

        # 날씨 정보 조회
        weather_text = ""
        try:
            if place.get('mapy') and place.get('mapx'):
                grid = convert_to_grid(float(place['mapy']), float(place['mapx']))
                weather_data = await get_current_weather(grid['nx'], grid['ny'])
                if weather_data:
                    temp = weather_data.get('temperature', '-')
                    sky_code = weather_data.get('skyStatus', '0')
                    pty_map = {'0': '맑음', '1': '비', '2': '비/눈', '3': '눈', '5': '빗방울', '6': '진눈깨비', '7': '눈날림'}
                    sky = pty_map.get(sky_code, '알수없음')
                    weather_text = f"\n현재 날씨: 기온 {temp}°C, 상태 {sky}"
        except Exception as w_e:
            print(f"Weather fetch failed: {w_e}")

        prompt = f"""다음 장소를 {request.max_length}자 내외로 요약하세요.
        장소명: {place['place_name']}
        설명: {place.get('overview', '')}
        리뷰: {reviews_text}{weather_text}
        요약 문구 (날씨를 고려하여 방문객에게 팁을 한 문장 포함할 것):"""

        async def summary_streamer():
            try:
                response_stream = await openai_client.chat.completions.create(
                    model="gpt-4o-mini",
                    messages=[{"role": "user", "content": prompt}],
                    max_tokens=500,
                    temperature=0.8,
                    stream=True
                )
                async for chunk in response_stream:
                    if chunk.choices and chunk.choices[0].delta.content:
                        yield chunk.choices[0].delta.content
            except Exception as e:
                traceback.print_exc()
                yield f" [에러 발생: {str(e)}]"

        return StreamingResponse(summary_streamer(), media_type="text/plain")
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/weather")
async def get_weather(request: WeatherRequest):
    """
    위경도를 받아 날씨 정보를 반환
    """
    try:
        grid = convert_to_grid(request.lat, request.lon)
        weather_data = await get_current_weather(grid['nx'], grid['ny'])
        
        if not weather_data:
            return {"message": "날씨 정보를 가져올 수 없습니다.", "data": None}
            
        return weather_data
    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/generate-intro")
async def generate_intro(request: IntroRequest, db: Session = Depends(get_db)):
    """
    관광지 정보 + 날씨 기반 AI 소개 문구 생성
    """
    try:
        # 1. 장소 정보 조회 (DB 우선)
        place_name = request.place_name
        overview = request.overview
        
        if request.place_id:
            place = get_place_info(db, request.place_id)
            if place:
                place_name = place['place_name']
                overview = place.get('overview', '')

        if not place_name:
            raise HTTPException(status_code=400, detail="장소 정보가 부족합니다.")

        # 2. 날씨 정보 조회
        weather_text = "날씨 정보 없음"
        weather_data = None
        
        if request.lat and request.lon:
            grid = convert_to_grid(request.lat, request.lon)
            weather_data = await get_current_weather(grid['nx'], grid['ny'])
            
            if weather_data:
                temp = weather_data.get('temperature', '-')
                sky_code = weather_data.get('skyStatus', '0') # PTY
                
                # 강수형태 코드 변환
                pty_map = {
                    '0': '맑음/비안옴', '1': '비', '2': '비/눈', 
                    '3': '눈', '5': '빗방울', '6': '빗방울눈날림', '7': '눈날림'
                }
                sky = pty_map.get(sky_code, '알수없음')
                weather_text = f"기온 {temp}°C, 상태 {sky}"

        # 3. 프롬프트 구성
        prompt = f"""
        당신은 여행 가이드입니다. 다음 정보를 바탕으로 관광지 소개 문구를 매력적으로 작성해주세요.
        현재 날씨 상황을 고려하여 방문객에게 도움이 되는 멘트를 한 문장 포함하세요.
        
        관광지: {place_name}
        설명: {overview}
        현재 날씨: {weather_text}
        
        작성 조건:
        - SNS 스타일로 친근하게
        - 이모지 적절히 사용
        - 300자 이내
        """

        # 4. AI 생성
        response = await openai_client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[{"role": "user", "content": prompt}],
            max_tokens=400,
            temperature=0.7
        )
        
        generated_text = response.choices[0].message.content
        
        return {
            "generated_text": generated_text,
            "weather": weather_data  # 날씨 정보도 함께 반환
        }

    except Exception as e:
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=9000)