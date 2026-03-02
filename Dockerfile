# build stage
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# 1) wrapper + gradle 설정 파일만 먼저 복사(캐시 최적화)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle* settings.gradle* ./

RUN chmod +x ./gradlew

RUN ./gradlew --no-daemon dependencies || true

# 2) 소스 복사 후 빌드
COPY src/ src/
RUN ./gradlew clean bootJar -x test --no-daemon

RUN cp build/libs/*.jar /app/app.jar

# run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]