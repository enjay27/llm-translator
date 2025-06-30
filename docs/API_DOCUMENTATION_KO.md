# LLM 번역기 API 문서

이 문서는 LLM 번역기 서비스의 API 엔드포인트에 대한 상세 정보를 제공합니다.

## 기본 URL

모든 API 요청의 기본 URL은 다음과 같습니다:

```
http://localhost:8080
```

## 인증

현재 버전에서는 별도의 인증이 필요하지 않습니다.

## API 엔드포인트

### 1. 번역 요청

텍스트를 여러 언어로 번역합니다.

**URL**: `/llm/{conversationId}`

**메소드**: `POST`

**URL 파라미터**:
- `conversationId` (필수): 대화 식별자. 새로운 대화를 시작하거나 기존 대화를 계속할 수 있습니다.

**요청 본문**:

```json
{
  "text": "번역할 텍스트",
  "targetLanguages": ["en", "ko", "cn", "jp"]
}
```

**파라미터 설명**:
- `text` (필수): 번역할 텍스트
- `targetLanguages` (선택): 번역할 대상 언어 코드 목록
  - `en`: 영어
  - `ko`: 한국어
  - `cn`: 중국어
  - `jp`: 일본어
  - 생략 시 모든 언어로 번역됩니다.

**응답**:

```json
{
  "result": {
    "output": {
      "text": "{\n  \"en\": \"Hello, how are you?\",\n  \"ko\": \"안녕하세요, 어떻게 지내세요?\",\n  \"cn\": \"你好，你好吗？\",\n  \"jp\": \"こんにちは、お元気ですか？\"\n}"
    }
  }
}
```

**응답 코드**:
- `200 OK`: 요청 성공
- `400 Bad Request`: 잘못된 요청 형식
- `500 Internal Server Error`: 서버 오류

### 2. 대화 기록 조회

특정 대화 ID에 대한 대화 기록을 조회합니다.

**URL**: `/llm/{conversationId}`

**메소드**: `GET`

**URL 파라미터**:
- `conversationId` (필수): 조회할 대화 ID

**응답**:

```json
{
  "messages": [
    {
      "type": "USER",
      "text": "Hello"
    },
    {
      "type": "ASSISTANT",
      "text": "{\n  \"en\": \"Hello\",\n  \"ko\": \"안녕하세요\",\n  \"cn\": \"你好\",\n  \"jp\": \"こんにちは\"\n}"
    },
    {
      "type": "USER",
      "text": "How are you?"
    },
    {
      "type": "ASSISTANT",
      "text": "{\n  \"en\": \"I'm fine, thank you.\",\n  \"ko\": \"저는 잘 지내요, 감사합니다.\",\n  \"cn\": \"我很好，谢谢。\",\n  \"jp\": \"元気です、ありがとう。\"\n}"
    }
  ]
}
```

**응답 코드**:
- `200 OK`: 요청 성공
- `404 Not Found`: 대화 ID를 찾을 수 없음

## 에러 응답

에러가 발생할 경우, 다음과 같은 형식으로 응답합니다:

```json
{
  "timestamp": "2023-07-01T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "에러 메시지",
  "path": "/llm/123"
}
```

## 예제

### 번역 요청 예제

**요청**:

```bash
curl -X POST "http://localhost:8080/llm/conversation123" \
     -H "Content-Type: application/json" \
     -d '{
           "text": "안녕하세요",
           "targetLanguages": ["en", "jp"]
         }'
```

**응답**:

```json
{
  "result": {
    "output": {
      "text": "{\n  \"en\": \"Hello\",\n  \"jp\": \"こんにちは\"\n}"
    }
  }
}
```

### 대화 기록 조회 예제

**요청**:

```bash
curl -X GET "http://localhost:8080/llm/conversation123"
```

**응답**:

```json
{
  "messages": [
    {
      "type": "USER",
      "text": "안녕하세요"
    },
    {
      "type": "ASSISTANT",
      "text": "{\n  \"en\": \"Hello\",\n  \"jp\": \"こんにちは\"\n}"
    }
  ]
}
```