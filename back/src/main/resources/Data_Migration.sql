-- 💡 1. 핫플레이스 테이블 비우기 (테스트 시)
-- TRUNCATE TABLE ssafy_trip.hotplace;

-- 💡 2. 대량 데이터 마이그레이션
INSERT INTO ssafy_trip.hotplace (
    place_name, 
    category, 
    address, 
    pos, 
    api_source, 
    is_pet_friendly, 
    avg_rating
)
SELECT 
    a.title AS place_name,
    -- contenttypes 테이블과 JOIN하여 명칭(Name)을 category로 사용
    t.content_type_name AS category, 
    a.addr1 AS address,
    
    -- 🔑 핵심 GIS 변환: ST_GeomFromText를 사용하여 POINT 객체를 생성
    -- CONCAT으로 'POINT(경도 위도)' WKT 문자열을 만들고, SRID 4326(WGS84)을 지정합니다.
    ST_GeomFromText(CONCAT('POINT(', a.longitude, ' ', a.latitude, ')'), 4326) AS pos,
    
    '공공 데이터' AS api_source, -- 출처는 더미 데이터로 명시
    1 AS is_pet_friendly,      -- 초기값 true
    0.0 AS avg_rating           -- 초기값
FROM 
    ssafy_trip.attractions a
LEFT JOIN 
    ssafy_trip.contenttypes t ON a.content_type_id = t.content_type_id
-- 💡 (옵션) 데이터가 없는 경우를 제외하려면 WHERE a.longitude IS NOT NULL AND a.latitude IS NOT NULL
;