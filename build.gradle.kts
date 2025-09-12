plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.22.0"
}

group = "com.devtracker"
version = "0.0.1-SNAPSHOT"
description = "DevTracker Backend API"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {

    // starter
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // JWT 관련
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")

    // QueryDSL
    implementation("com.querydsl:querydsl-jpa:5.0.0:jakarta")
    implementation("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("com.querydsl:querydsl-apt:5.0.0:jakarta")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Database
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.zaxxer:HikariCP")

    // HTTP 클라이언트 (GitHub API 호출용)
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // JSON 처리
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // 개발 편의성
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // API 문서화 (Swagger/OpenAPI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")

    // 테스트 의존성
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:testcontainers")
    testRuntimeOnly("com.h2database:h2") // 테스트용 인메모리 DB
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

spotless {
    // 공통 적용
    kotlinGradle {
        target("**/*.gradle.kts")
        ktlint("0.50.0") // Kotlin Gradle DSL 포맷팅
    }

    // Java 포맷팅
    java {
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        googleJavaFormat("1.17.0") // Google Java Format
        removeUnusedImports() // 사용하지 않는 import 제거
    }

    // Kotlin 소스코드 포맷팅 (있으면)
    kotlin {
        target("src/main/kotlin/**/*.kt", "src/test/kotlin/**/*.kt")
        ktlint("0.50.0")
        licenseHeaderFile(rootProject.file("spotless/HEADER.txt")) // 파일 상단에 라이선스 추가
    }

    // 기타 리소스나 설정 파일
    format("misc") {
        target("**/*.gradle", "**/*.md", "**/*.yml", "**/*.yaml", "**/*.properties")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
