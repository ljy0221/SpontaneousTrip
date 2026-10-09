-- hotplace 반경 검색(MBRContains + ST_Distance_Sphere)이 공간 인덱스를 타도록 하는 DDL.
-- 전제: Data_Migration_fixed_latlon.sql 로 pos 를 모든 행에 채운 뒤 실행한다(NOT NULL 필요).
-- 조건 세 가지가 모두 필요하다. SRID 속성이 없으면 인덱스를 만들어도 옵티마이저가 쓰지 않는다(EXPLAIN type=ALL).
--   1) pos NOT NULL   2) pos SRID 4326   3) SPATIAL INDEX
-- 검증(재현 실험): MySQL 8.4.11, 50,915행, 반경 5km 첫 페이지 191.5ms -> 14.7ms, EXPLAIN type=range.
ALTER TABLE hotplace MODIFY pos POINT NOT NULL SRID 4326;
ALTER TABLE hotplace ADD SPATIAL INDEX sidx_hotplace_pos (pos);
