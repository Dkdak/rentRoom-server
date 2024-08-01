
##...create a new repository on the command line
```sh
echo "# test" >> README.md
git init
git add README.md
git commit -m "first commit"
git branch -M main
git remote add origin https://github.com/Dkdak/rentRoom-server.git
git push -u origin main
```


# 로컬 개발 환경 설정
이 문서에서는 Docker와 Docker Compose를 사용하여 Gradle Spring Boot 프로젝트와 PostgreSQL을 로컬 환경에서 설정하는 방법을 설명합니다.
## 기본설정

### 1. 프로젝트 구조
프로젝트 기본 구조를 아래와 같습니다. 
```css
my-project
├── backend
│   ├── Dockerfile
│   ├── build.gradle
│   ├── settings.gradle
│   ├── gradlew
│   ├── gradle
│   │   └── wrapper
│   │       ├── gradle-wrapper.jar
│   │       └── gradle-wrapper.properties
│   └── src
└── docker-compose.yml
```

### 2. 프로젝트 클론
```bash
cd C:/my-project/backend
git clone https://github.com/yourusername/your-repo.git .
```


# 1. docker spring boot에서 로컬 postgresql db 에 접속
docker 위에 spring boot 를 올리고, 로컬에 설치되어 있는 postgresql db 에 접속합니다. 







## 1.2. docker-compose 설정
docker-compose.yml 파일을 다음과 같이 작성합니다.
```yaml
version: '3'
services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: spring_backend
    ports:
      - "9192:9192"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/postgres
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=pgadmin
    networks:
      - my_network

networks:
  my_network:
    driver: bridge
```

- host.docker.internal을 사용하여 Docker 컨테이너에서 호스트 머신의 데이터베이스에 접근할 수 있습니다. host.docker.internal은 Docker가 컨테이너에서 호스트 시스템에 접근하기 위해 제공하는 도메인입니다. 이 도메인을 사용하면 컨테이너가 호스트의 localhost에 접근할 수 있습니다.


## 1.3. Spring Boot 애플리케이션 설정
application.yml 또는 application.properties 파일에서 환경 변수를 사용하려면, 해당 파일에서 환경 변수를 참조하는 형식으로 설정할 수 있습니다.
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driverClassName: org.postgresql.Driver
    hikari:
      auto-commit: false
```
- 여기서 ${} 구문은 Spring Boot에서 환경 변수를 참조하는 방법입니다.


## 1.4. Dockerfile 설정
1. build의 bootJar를 실행하여 jar파일 생성합니다. (gradle/tasks/build/bootJar 실행)
2. Dockerfile에서 실제 JAR 파일 경로를 참조하도록 설정합니다. 
```Dockerfile
FROM openjdk:17-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```


## 1.5. docker build up
```bash
docker-compose down
docker-compose up --build
```




