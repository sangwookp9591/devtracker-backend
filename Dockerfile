# 빌드 단계
FROM gradle:8-jdk21 AS builder

WORKDIR /app

# Gradle 설정 파일들 먼저 복사 (캐싱 최적화)
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle/ gradle/

# 의존성 다운로드
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src/ src/

# 애플리케이션 빌드
RUN gradle bootJar --no-daemon

# 실행 단계 - Java 21 JRE 사용
FROM eclipse-temurin:21-jre

WORKDIR /app

# 필요한 도구 설치
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 애플리케이션용 사용자 생성
RUN groupadd -r devtracker && useradd -r -g devtracker devtracker

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 로그 디렉토리 생성 및 권한 설정
RUN mkdir -p /app/logs && \
    chown -R devtracker:devtracker /app

# 사용자 변경 (root → 일반 유저)
USER devtracker

# 실행
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
