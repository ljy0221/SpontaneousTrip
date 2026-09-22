# 1. 가벼운 Python 3.11 슬림 이미지를 기반으로 사용
FROM python:3.11-slim

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 시스템 의존성 설치 (빌드 시 필요한 최소 도구)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# 4. 🔥 핵심: PyTorch CPU 버전 전용 인덱스에서 먼저 설치
# 이 한 줄로 이미지 용량이 약 2~3GB 이상 줄어듭니다.
RUN pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

# 5. 나머지 패키지 설치를 위한 파일 복사
# (app/ 폴더 안에 requirements.txt가 있는 구조를 유지합니다)
COPY app/requirements.txt .

# 6. 나머지 라이브러리 설치
RUN pip install --no-cache-dir -r requirements.txt

# 7. 임베딩 모델 미리 다운로드 (선택 사항이나 권장)
# 컨테이너 실행 시 매번 다운로드하는 것을 방지합니다.
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('jhgan/ko-sroberta-multitask')"

# 8. 소스 코드 복사
COPY app/ .

# 9. 포트 설정 및 실행
EXPOSE 9000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "9000"]