# LLM 번역기 설치 및 설정 가이드

이 가이드는 LLM 번역기 서비스를 설치하고 실행하는 방법에 대한 상세 정보를 제공합니다.

## 목차

1. [필수 조건](#필수-조건)
2. [설치 과정](#설치-과정)
3. [설정 방법](#설정-방법)
4. [실행 방법](#실행-방법)
5. [문제 해결](#문제-해결)

## 필수 조건

LLM 번역기를 실행하기 위해 다음 소프트웨어가 필요합니다:

- **Java 17** 이상
- **Docker** 및 **Docker Compose**
- **Git** (소스 코드 다운로드용)
- **Ollama** 서버 접근 권한 (또는 자체 Ollama 서버 설정)

### Java 설치

#### Windows
1. [AdoptOpenJDK](https://adoptopenjdk.net/) 또는 [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html)에서 Java 17 이상 버전을 다운로드하여 설치합니다.
2. 시스템 환경 변수에 `JAVA_HOME`을 설정하고 `Path` 변수에 `%JAVA_HOME%\bin`을 추가합니다.

#### macOS
Homebrew를 사용하여 설치:
```bash
brew install openjdk@17
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

### Docker 및 Docker Compose 설치

#### Windows/macOS
[Docker Desktop](https://www.docker.com/products/docker-desktop)을 다운로드하여 설치합니다.

#### Linux
Docker 설치:
```bash
sudo apt update
sudo apt install docker.io
sudo systemctl enable --now docker
```

Docker Compose 설치:
```bash
sudo apt install docker-compose
```

## 설치 과정

1. 저장소 클론:
   ```bash
   git clone <repository-url>
   cd llm-translator
   ```

2. Gradle 래퍼를 사용하여 프로젝트 빌드:
   ```bash
   ./gradlew build -x test
   ```
   
   Windows에서는:
   ```bash
   gradlew.bat build -x test
   ```

## 설정 방법

### 데이터베이스 설정

기본적으로 애플리케이션은 MySQL 데이터베이스를 사용합니다. Docker Compose를 통해 자동으로 설정되지만, 필요한 경우 `application.yml` 파일에서 데이터베이스 설정을 변경할 수 있습니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/llm_service
    username: root
    password: root
```

### Ollama 설정

애플리케이션은 Ollama 서버를 사용하여 LLM 모델에 접근합니다. `application.yml` 파일에서 Ollama 서버 URL과 모델을 설정할 수 있습니다:

```yaml
spring:
  ai:
    ollama:
      base-url: http://enjay.myds.me:11434  # Ollama 서버 URL
      chat:
        model: qwen3:14b  # 사용할 모델
```

자체 Ollama 서버를 설정하려면:

1. [Ollama 공식 사이트](https://ollama.ai/)에서 Ollama를 설치합니다.
2. 필요한 모델을 다운로드합니다:
   ```bash
   ollama pull qwen3:14b
   ```
3. `application.yml` 파일에서 `base-url`을 `http://localhost:11434`로 변경합니다.

## 실행 방법

1. Docker Compose로 MySQL 데이터베이스 실행:
   ```bash
   docker-compose up -d
   ```

2. Spring Boot 애플리케이션 실행:
   ```bash
   ./gradlew bootRun
   ```
   
   Windows에서는:
   ```bash
   gradlew.bat bootRun
   ```

3. 애플리케이션은 기본적으로 http://localhost:8080 에서 실행됩니다.

4. Swagger UI를 통해 API 문서에 접근할 수 있습니다:
   http://localhost:8080/swagger-ui.html

## 문제 해결

### 데이터베이스 연결 오류

**문제**: 애플리케이션이 데이터베이스에 연결할 수 없습니다.

**해결 방법**:
1. MySQL 컨테이너가 실행 중인지 확인:
   ```bash
   docker ps
   ```

2. MySQL 컨테이너가 실행 중이 아니라면 다시 시작:
   ```bash
   docker-compose up -d
   ```

3. MySQL 로그 확인:
   ```bash
   docker-compose logs mysql
   ```

4. `application.yml`의 데이터베이스 설정이 올바른지 확인합니다.

### Ollama 서버 연결 오류

**문제**: 애플리케이션이 Ollama 서버에 연결할 수 없습니다.

**해결 방법**:
1. `application.yml`에 설정된 Ollama 서버 URL이 올바른지 확인합니다.
2. 자체 Ollama 서버를 사용하는 경우, Ollama 서비스가 실행 중인지 확인합니다.
3. 방화벽 설정을 확인하여 Ollama 서버 포트(기본값: 11434)가 열려 있는지 확인합니다.

### 애플리케이션 시작 실패

**문제**: 애플리케이션이 시작되지 않습니다.

**해결 방법**:
1. 로그를 확인하여 오류 메시지를 확인합니다:
   ```bash
   ./gradlew bootRun --debug
   ```

2. Java 버전이 17 이상인지 확인합니다:
   ```bash
   java -version
   ```

3. 필요한 모든 의존성이 다운로드되었는지 확인합니다:
   ```bash
   ./gradlew --refresh-dependencies
   ```

### 번역 결과가 올바르지 않음

**문제**: 번역 결과가 예상과 다르거나 오류가 발생합니다.

**해결 방법**:
1. Ollama 서버가 올바르게 설정되어 있는지 확인합니다.
2. 사용 중인 모델(qwen3:14b)이 Ollama 서버에 설치되어 있는지 확인합니다.
3. 요청 형식이 올바른지 확인합니다.
4. 애플리케이션 로그를 확인하여 오류 메시지를 확인합니다.

## 추가 지원

문제가 계속되면 다음 연락처로 지원을 요청하세요:
- 이메일: support@example.com