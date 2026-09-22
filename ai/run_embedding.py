#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
SSAFY Pass - 데이터 준비 & 임베딩 (배치 실행용)

실행 방법:
  python run_embedding.py

백그라운드 실행:
  nohup python run_embedding.py > embedding.log 2>&1 &
"""

import pymysql
import pandas as pd
from tqdm import tqdm
import json
import os
from datetime import datetime
from sentence_transformers import SentenceTransformer
import chromadb
from chromadb.config import Settings
from dotenv import load_dotenv

# ============================================================
# 설정
# ============================================================

load_dotenv()

DB_CONFIG = {
    'host': os.getenv("DB_HOST"),
    'user': os.getenv("DB_USER"),
    'password': os.getenv("DB_PASSWORD"), # 비밀번호 입력
    'database': os.getenv("DB_NAME"),
    'charset': 'utf8mb4',
    'cursorclass': pymysql.cursors.DictCursor
}

DATA_DIR = './data'
VECTOR_DB_PATH = './data/vectordb'
BACKUP_FILE = './data/documents.json'

# ============================================================
# 로깅
# ============================================================

def log(message):
    """타임스탬프와 함께 로그 출력"""
    timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    print(f"[{timestamp}] {message}")

# ============================================================
# 1. MySQL 연결
# ============================================================

log("=" * 60)
log("Step 1: MySQL 연결")
log("=" * 60)

try:
    connection = pymysql.connect(**DB_CONFIG)
    log("✅ DB 연결 성공!")
    
    # 데이터 확인
    with connection.cursor() as cursor:
        cursor.execute("SELECT COUNT(*) as cnt FROM hotplace")
        place_count = cursor.fetchone()['cnt']
        
        cursor.execute("SELECT COUNT(*) as cnt FROM review")
        review_count = cursor.fetchone()['cnt']
        
        cursor.execute("SELECT COUNT(*) as cnt FROM users")
        user_count = cursor.fetchone()['cnt']
    
    log(f"📊 데이터 현황:")
    log(f"   장소: {place_count:,}개")
    log(f"   리뷰: {review_count:,}개")
    log(f"   사용자: {user_count:,}명")
    
except Exception as e:
    log(f"❌ DB 연결 실패: {e}")
    exit(1)

# ============================================================
# 2. 문서 생성
# ============================================================

log("\n" + "=" * 60)
log("Step 2: RAG 문서 생성")
log("=" * 60)

documents = []

try:
    with connection.cursor() as cursor:
        cursor.execute("SELECT * FROM hotplace")
        places = cursor.fetchall()
        
        log(f"📝 {len(places):,}개 장소 처리 중...")
        
        for place in tqdm(places, desc="문서 생성"):
            place_id = place['place_id']
            
            # 장소 정보 문서
            doc_text = f"""
                {place['place_name']}
                카테고리: {place['category']}
                주소: {place['address']}
                평점: {place['avg_rating']}/5.0
                반려동물 동반: {'가능' if place['is_pet_friendly'] else '불가'}
                출처: {place['api_source']}
                """
            if place.get('overview'):
                doc_text += f"\n설명: {place['overview']}"
            
            doc_text = doc_text.strip()
            
            documents.append({
                "content": doc_text,
                "metadata": {
                    "place_id": place_id,
                    "place_name": place['place_name'],
                    "type": "place_info",
                    "category": place['category']
                }
            })
            
            # 리뷰 문서
            cursor.execute("""
                SELECT r.*, u.nickname
                FROM review r
                LEFT JOIN users u ON r.user_id = u.user_id
                WHERE r.place_id = %s
                ORDER BY r.rating DESC, r.created_at DESC
                LIMIT 5
            """, (place_id,))
            reviews = cursor.fetchall()
            
            for review in reviews:
                doc_text = f"""
                    {place['place_name']} 방문 후기
                    평점: {review['rating']}/5
                    작성자: {review.get('nickname', 'Unknown')}
                    내용: {review['content']}
                    """.strip()
                
                documents.append({
                    "content": doc_text,
                    "metadata": {
                        "place_id": place_id,
                        "place_name": place['place_name'],
                        "type": "review",
                        "rating": review['rating']
                    }
                })
    
    log(f"✅ 총 {len(documents):,}개 문서 생성 완료!")
    
    # 백업 저장
    os.makedirs(DATA_DIR, exist_ok=True)
    with open(BACKUP_FILE, 'w', encoding='utf-8') as f:
        json.dump(documents, f, ensure_ascii=False, indent=2)
    
    log(f"💾 백업 저장: {BACKUP_FILE}")
    log(f"   파일 크기: {os.path.getsize(BACKUP_FILE) / 1024 / 1024:.2f} MB")
    
except Exception as e:
    log(f"❌ 문서 생성 실패: {e}")
    connection.close()
    exit(1)

# ============================================================
# 3. 임베딩 모델 로드
# ============================================================

log("\n" + "=" * 60)
log("Step 3: 임베딩 모델 로드")
log("=" * 60)

try:
    log("📥 모델 다운로드 중... (jhgan/ko-sroberta-multitask)")
    embedding_model = SentenceTransformer('jhgan/ko-sroberta-multitask')
    log("✅ 모델 로드 완료!")
    log("   임베딩 차원: 768")
    log("   모드: CPU")
    
except Exception as e:
    log(f"❌ 모델 로드 실패: {e}")
    connection.close()
    exit(1)

# ============================================================
# 4. ChromaDB 초기화
# ============================================================

log("\n" + "=" * 60)
log("Step 4: ChromaDB 초기화")
log("=" * 60)

try:
    client = chromadb.PersistentClient(
        path=VECTOR_DB_PATH,
        settings=Settings(anonymized_telemetry=False)
    )
    
    # 기존 컬렉션 삭제
    try:
        client.delete_collection(name="ssafy_places")
        log("   기존 컬렉션 삭제")
    except:
        pass
    
    # 새 컬렉션 생성
    collection = client.create_collection(
        name="ssafy_places",
        metadata={
            "description": "SSAFY Pass 장소 정보",
            "created_at": datetime.now().isoformat()
        }
    )
    
    log(f"✅ ChromaDB 초기화 완료!")
    log(f"   저장 경로: {VECTOR_DB_PATH}")
    
except Exception as e:
    log(f"❌ ChromaDB 초기화 실패: {e}")
    connection.close()
    exit(1)

# ============================================================
# 5. 임베딩 생성 및 저장
# ============================================================

log("\n" + "=" * 60)
log("Step 5: 임베딩 생성 (CPU 모드)")
log("=" * 60)

batch_size = 32
total_batches = (len(documents) + batch_size - 1) // batch_size

log(f"📊 설정:")
log(f"   총 문서: {len(documents):,}개")
log(f"   배치 크기: {batch_size}")
log(f"   총 배치: {total_batches:,}개")
log(f"   예상 시간: {len(documents) * 0.1 / 60:.1f}분")

import time
start_time = time.time()

try:
    for batch_idx in tqdm(range(total_batches), desc="임베딩 진행"):
        start_idx = batch_idx * batch_size
        end_idx = min((batch_idx + 1) * batch_size, len(documents))
        batch_docs = documents[start_idx:end_idx]
        
        # 임베딩 생성
        contents = [doc['content'] for doc in batch_docs]
        embeddings = embedding_model.encode(
            contents,
            show_progress_bar=False,
            convert_to_numpy=True
        )
        
        # ChromaDB 저장
        collection.add(
            embeddings=embeddings.tolist(),
            documents=contents,
            metadatas=[doc['metadata'] for doc in batch_docs],
            ids=[f"doc_{start_idx + i}" for i in range(len(batch_docs))]
        )
        
        # 진행 상황 로그 (10% 단위)
        if (batch_idx + 1) % max(1, total_batches // 10) == 0:
            progress = (batch_idx + 1) / total_batches * 100
            elapsed = time.time() - start_time
            remaining = elapsed / (batch_idx + 1) * (total_batches - batch_idx - 1)
            log(f"   진행: {progress:.1f}% | 경과: {elapsed/60:.1f}분 | 남은시간: {remaining/60:.1f}분")
    
    elapsed = time.time() - start_time
    
    log("\n✅ 임베딩 완료!")
    log(f"   총 시간: {elapsed/60:.1f}분 ({elapsed:.1f}초)")
    log(f"   속도: {len(documents)/elapsed:.1f} 문서/초")
    log(f"   저장된 문서: {collection.count():,}개")
    
except Exception as e:
    log(f"❌ 임베딩 실패: {e}")
    connection.close()
    exit(1)

# ============================================================
# 6. 검색 테스트
# ============================================================

log("\n" + "=" * 60)
log("Step 6: 검색 테스트")
log("=" * 60)

try:
    test_queries = [
        "서울 데이트 코스 추천",
        "맛집 추천",
        "반려동물 동반 가능한 곳"
    ]
    
    for query in test_queries:
        log(f"\n🔍 검색: '{query}'")
        query_embedding = embedding_model.encode(query)
        results = collection.query(
            query_embeddings=[query_embedding.tolist()],
            n_results=3
        )
        
        for i, (doc, metadata) in enumerate(zip(results['documents'][0], results['metadatas'][0])):
            log(f"   {i+1}. {metadata['place_name']} ({metadata['type']})")
    
    log("\n✅ 검색 테스트 완료!")
    
except Exception as e:
    log(f"⚠️  검색 테스트 실패: {e}")

# ============================================================
# 7. 정리
# ============================================================

log("\n" + "=" * 60)
log("작업 완료!")
log("=" * 60)

log(f"\n📊 최종 통계:")
log(f"   총 문서: {collection.count():,}개")
log(f"   장소: {place_count:,}개")
log(f"   리뷰: {review_count:,}개")
log(f"   임베딩 시간: {elapsed/60:.1f}분")

log(f"\n📁 생성된 파일:")
log(f"   {BACKUP_FILE}")
log(f"   {VECTOR_DB_PATH}/")

log(f"\n✅ Day 1-2 완료!")
log(f"   다음: Day 3-4 (RAG 챗봇 구현)")

# DB 연결 종료
connection.close()
log("\n✅ MySQL 연결 종료")

log("\n" + "=" * 60)
log(f"종료 시각: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
log("=" * 60)
