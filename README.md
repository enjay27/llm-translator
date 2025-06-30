# LLM 번역기 서비스

LLM 번역기는 대규모 언어 모델(LLM)을 사용하여 텍스트를 여러 언어로 번역하는 Spring Boot 애플리케이션입니다.

## 기능

- 텍스트를 영어, 한국어, 중국어, 일본어로 번역
- 대화 기록 저장 및 조회
- RESTful API 인터페이스
- Swagger UI를 통한 API 문서화

## 문서

- [API 문서](docs/API_DOCUMENTATION_KO.md) - API 엔드포인트에 대한 상세 정보
- [설치 및 설정 가이드](docs/SETUP_GUIDE_KO.md) - 상세한 설치 및 설정 방법

## 기술 스택

- Java 17+
- Spring Boot
- Spring AI
- Ollama LLM 모델 (qwen3:14b)
- MySQL 8.0
- Docker & Docker Compose
- Swagger/OpenAPI

## 시작하기

### 필수 조건

- Java 17 이상
- Docker 및 Docker Compose
- Ollama 서버 접근 권한

### 설치 및 실행

1. 저장소 클론:
   ```bash
   git clone <repository-url>
   cd llm-translator
   ```

2. Docker Compose로 MySQL 실행:
   ```bash
   docker-compose up -d
   ```

3. 애플리케이션 빌드 및 실행:
   ```bash
   ./gradlew bootRun
   ```

4. 애플리케이션은 기본적으로 http://localhost:8080 에서 실행됩니다.

## API 사용법

### API 문서

Swagger UI를 통해 API 문서에 접근할 수 있습니다:
- http://localhost:8080/swagger-ui.html

### 주요 엔드포인트

#### 번역 요청

```
POST /llm/{conversationId}
```

요청 본문:
```json
{
  "text": "번역할 텍스트",
  "targetLanguages": ["en", "ko", "cn", "jp"]
}
```

- `text`: 번역할 텍스트
- `targetLanguages`: 번역할 대상 언어 목록 (생략 시 모든 언어로 번역)

#### 대화 기록 조회

```
GET /llm/{conversationId}
```

- `conversationId`: 조회할 대화 ID

## 설정

`application.yml` 파일에서 다음 설정을 변경할 수 있습니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/llm_service
    username: root
    password: root
  ai:
    ollama:
      base-url: http://enjay.myds.me:11434
      chat:
        model: qwen3:14b
```

## 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 LICENSE 파일을 참조하세요.

## 연락처

- LLM 번역기 팀
- 이메일: support@example.com
