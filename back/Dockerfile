# 1단계: 빌드 스테이지
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# 의존성 파일을 먼저 복사하여 캐시 활용
COPY pom.xml .
RUN mvn dependency:go-offline

# 소스 복사 및 빌드 (테스트 제외)
COPY src ./src
RUN mvn clean package -DskipTests

# 2단계: 실행 스테이지
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# 빌드 스테이지에서 생성된 jar 파일을 복사
# 프로젝트 이름에 따라 jar 파일명이 달라질 수 있으니 확인 후 수정하세요.
COPY --from=build /app/target/*.jar app.jar

EXPOSE 9001
ENTRYPOINT ["java", "-jar", "app.jar"]