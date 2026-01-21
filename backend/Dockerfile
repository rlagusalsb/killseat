#1. 빌드 스테이지
FROM gradle:8-jdk21 AS build
WORKDIR /app

#의존성 설치 캐시를 위해 설정 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

#실제 소스 코드 복사
COPY src src

#빌드
RUN ./gradlew clean bootJar -x test

#2. 실행 스테이지
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

#환경 설정 파일 복사
COPY .env .env
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]