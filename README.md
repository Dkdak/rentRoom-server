
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
## 1. 기본설정

1) 프로젝트 구조
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

2) 프로젝트 클론
```bash
cd C:/my-project/backend
git clone https://github.com/yourusername/your-repo.git .
```
<br>

3) spring boot 프로젝트에서 환경 변수를 참조
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

<br>

# 2. docker spring boot에서 로컬 postgresql db 에 접속
docker 위에 spring boot 를 올리고, 로컬에 설치되어 있는 postgresql db 에 접속합니다. 

### 2.1. docker-compose 설정
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


### 2.2. Dockerfile 설정
1. build의 bootJar를 실행하여 jar파일 생성합니다. (gradle/tasks/build/bootJar 실행)
2. Dockerfile에서 실제 JAR 파일 경로를 참조하도록 설정합니다. 
```Dockerfile
FROM openjdk:17-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```


### 2.3. docker build up
```bash
docker-compose down
docker-compose up --build
```





<br>

# 3. docker spring boot, docker postgresql 를 설정하여 연결
docker spring boot에서 docker postgresql 에 접속합니다. 

### 3.1. docker-compose 설정
docker-compose.yml 파일을 다음과 같이 작성합니다.
```yaml
version: '3'
services:
  postgres:
    image: postgres:latest
    container_name: postgres_container
    environment:
      POSTGRES_DB: postgres
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: pgadmin
    ports:
      - "5434:5432" # 호스트와 컨테이너 포트 매핑
    networks:
      - my_network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: spring_backend
    ports:
      - "9192:9192"
    environment:
      -SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/postgres # 컨테이너 내부에서 PostgreSQL에 접근
      -SPRING_DATASOURCE_USERNAME: postgres
      -SPRING_DATASOURCE_PASSWORD: pgadmin
    depends_on:
      - postgres
    networks:
      - my_network

networks:
  my_network:
    driver: bridge
```
- 컨테이너 내부에서 PostgreSQL에 접근하기 위해 SPRING_DATASOURCE_URL을 jdbc:postgresql://postgres:5432/postgres로 설정합니다.



### 3.2. 오류 해결
1) connect refuse 관련 에러
- postgres 컨테이너가 떠 있는 상태에서 로컬에서 접속이 가능한데, spring docker에서 접속이 안된다면, 네트웍을 확인한다. 
```bash
docker network ls
docker network inspect <network_name>
ex ) <<my_project>> +_default
ex ) docker network inspect my_project_default
```
- 각각의 container가 Containers 안에 포함되어 있어야 한다. 그렇기 않은 경우 네크워크를 묶어준다.
```yaml
    networks:
      - my_network
```

2) connect refuse 관련 에러
- 5432 port는 이미 로컬에서 사용하는 postgresql port 임으로 다른 포트를 설정하고 매핑합니다. 
```yaml
  postgres:
    ports:
      - "5434:5432" # 호스트와 컨테이너 포트 매핑
```

3) connect refuse 관련 에러
- 컨테이너 내부에서 PostgreSQL에 접근함으로 내부 port로 설정합니다. 
```yaml
    environment:
      - SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/postgres
```





<br>

# 4. docker spring boot, postgresql 를 각각의 dockerfile을 설정하여 연결
1. 각각의 dockerfile을 참조하여 spring boot, postgresql을 빌드하고 연결합니다.
2. spring boot 는 gradle build를 통해서 자동으로 빌드하도록 설정합니다.

### 4.1. 파일 구조 변경
파일 구조를 다음과 같이 합니다. 
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
├── postgres
│   ├── init
│   │   ├── init.sh
│   │   └── init.sql
│   └── Dockerfile
│
└── docker-compose.yml
```

### 4.2. spring boot Dockerfile 설정
backend/Dockerfile을 다음과 같이 설정합니다.
```Dockerfile
# Stage 1: Build
FROM gradle:8.2-jdk17 AS builder
WORKDIR /app

# Gradle 파일 복사 및 의존성 다운로드
COPY build.gradle settings.gradle /app/
RUN gradle dependencies || true  # 의존성 다운로드를 위한 별도의 단계

# 애플리케이션 소스 코드 복사 및 빌드
COPY . /app/
RUN gradle build -x test --parallel

# Stage 2: Runtime
FROM openjdk:17-jdk-slim
WORKDIR /app
# Copy the JAR file from the build stage
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# Expose the port the app runs on
EXPOSE 9192

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
```

### 4.3. PostgreSQL Dockerfile 설정
postgres/Dockerfile을 다음과 같이 설정합니다.
```Dockerfile
FROM postgres:latest
```

### 4.4. docker-compose 설정
1) postgres port:5432는 이미 로컬에 설치되어 있다면, 호스트와 컨테이너 포트 매핑 합니다. 
2) backend에서 postgre접속시에 컨테이너 내부에서 PostgreSQL에 접근하는 것임으로 postgres:5432로 설정합니다. 
3) docker-compose.yml 파일을 다음과 같이 작성합니다.
```yaml
version: '3'
services:
  postgres:
    build:
      context: ./postgres
      dockerfile: Dockerfile
    container_name: postgres_container
    environment:
      POSTGRES_DB: postgres
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: pgadmin
    ports:
      - "5434:5432"  #호스트와 컨테이너 포트 매핑
    networks:
      - my_network
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./postgres/init:/docker-entrypoint-initdb.d

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: spring_backend
    ports:
      - "9192:9192"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postgres  # 컨테이너 내부에서 PostgreSQL에 접근
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=pgadmin
    depends_on:
      - postgres
    networks:
      - my_network

networks:
  my_network:
    driver: bridge

volumes:
  postgres_data:

```




