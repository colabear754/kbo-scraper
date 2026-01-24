# ⚾ KBO 경기 일정 수집 및 조회 서비스

[KBO 공식 웹사이트](https://www.koreabaseball.com/)의 경기 일정을 크롤링하여 데이터베이스에 저장하고, REST API를 통해 경기 정보를 조회할 수 있는 Spring Boot 기반의 백엔드 애플리케이션입니다.

## ⚠️ 주의사항

**본 프로젝트는 개인 학습 및 포트폴리오 목적으로 개발된 토이 프로젝트입니다.**

1. **저작권 및 데이터 소유권**: 크롤링한 데이터(경기 일정, 결과 등)의 모든 권리는 KBO(한국야구위원회)에 있습니다.
2. **상업적 이용 불가**: 본 프로젝트를 통해 수집된 데이터는 절대 상업적인 용도로 사용되어서는 안 됩니다.
3. **서버 부하 방지**: 과도한 요청으로 대상 서버에 부하를 주지 않도록 주의해야 하며, 실제 운영 환경이 아닌 로컬 또는 제한된 환경에서만 실행하는 것을 권장합니다.
4. **Robots.txt 준수**: 본 프로젝트는 KBO 웹사이트의 `robots.txt` 규약을 준수합니다. 수집 대상인 경기 일정 페이지(`/Schedule/Schedule.aspx`)는 크롤링이 허용된 경로임을 확인 후 진행하였습니다.

---

## 🏃‍♂️ 실행 방법
이 프로젝트는 **H2 Embedded Mode**&#8203;를 기본으로 사용하여, 별도의 데이터베이스 설치 없이 바로 실행 및 테스트가 가능합니다.

### 1. DB 연결 설정
기본적으로 `src/main/resources/application.yml`에 아래와 같이 H2 설정이 되어 있습니다. 프로젝트 실행 시 `./data/kbo-crawler` 경로에 DB 파일이 자동으로 생성됩니다.
```yaml
spring:
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:./data/kbo-crawler
    username: sa
    password:
```

> **Note**: 다른 DB를 사용하려면 위 설정을 해당 DB 정보로 수정해주세요.

### 2. 애플리케이션 실행
두 방법 중 편한 방법을 사용하여 실행할 수 있습니다.
* IDE 사용 시: IntelliJ IDEA 등의 IDE에서 프로젝트를 열고, **`KboScraperApplication.kt`** 파일을 실행하면 서버가 구동됩니다.
* 터미널 사용 시: 다음 명령어를 입력하여 프로젝트를 다운로드하고 실행할 수 있습니다.
```bash
git clone https://github.com/Colabear754/kbo-scraper.git
cd kbo-scraper
./gradlew bootRun
```

---

## 📝 프로젝트 소개

KBO 공식 홈페이지의 경기 일정 페이지의 경기 정보를 정형화된 데이터로 파싱하여 DB에 적재하고, 이를 클라이언트가 쉽게 조회할 수 있도록 API를 제공합니다.

### 주요 기능

* **경기 일정 크롤링 및 저장**
  * 특정 시즌(연도) 및 시리즈(시범경기/정규시즌/포스트시즌) 데이터를 수집합니다.
  * 코루틴을 통한 **비동기 병렬 처리(Async & Parallel Processing)**&#8203;를 적용하여 월별 데이터를 동시에 크롤링함으로써 수집 속도를 개선했습니다.
  * 기존 데이터가 존재할 경우 최신 정보로 덮어쓰는 **Upsert(Update + Insert)** 로직을 적용하여 데이터 중복을 방지하고 최신성을 유지합니다.
* **경기 정보 조회 API**
  * 특정 팀과 날짜를 기준으로 경기를 조회합니다.
  * 고유 경기 ID(Key)를 통한 단일 경기 상세 조회가 가능합니다.
* **개발 편의성 및 표준화**
  * `StringToEnumConverterFactory`를 구현하여 API 요청 시 대소문자 구분 없이 Enum 매핑을 지원합니다.
  * `ResponseBodyAdvice` 구현체를 통해 모든 API 응답을 통일된 포맷(`GlobalResponse`)으로 래핑하여 반환합니다.

---

## 🛠 기술 스택

* **Language**: Kotlin 2.2.20
* **Framework**: Spring Boot 3.4.11
* **Concurrency**: Kotlin Coroutines
* **Database**: Spring Data JPA, H2
* **Crawling**: Playwright
* **Test**: Kotest, MockK

---

## 🚀 아키텍처 및 핵심 로직

### 1. Kotlin Coroutines를 활용한 비동기 병렬 크롤링
KBO 경기 일정은 월별로 페이지가 분리되어 있어, 순차적으로 수집 시 많은 시간이 소요됩니다. 이를 해결하기 위해 **Kotlin Coroutines**를 도입했습니다.

*   **Non-blocking I/O**: 네트워크 요청 시 스레드를 점유하지 않고 `suspend` 되어 시스템 리소스를 효율적으로 사용합니다.
*   **Parallel Execution**: `async`와 `awaitAll`을 사용하여 각 월별 크롤링 작업을 병렬로 동시에 수행합니다.
*   **Dispatchers.IO**: 크롤링과 같은 I/O 작업에 최적화된 스레드 풀을 사용하여 메인 로직의 부하를 줄였습니다.

이를 통해 기존 동기 방식대비 크롤링 속도를 **약 3.6배 향상**했습니다.(73초 → 20초)

### 2. 유연한 크롤링 요청 처리
사용자는 시즌과 시리즈 종류를 선택적으로 요청할 수 있습니다.
* `seriesType` 값이 명시된 경우: 해당 시리즈만 수집
* `seriesType` 값이 `null`인 경우: 해당 시즌의 **모든 시리즈(시범경기, 정규시즌, 포스트시즌)**&#8203;를 자동으로 순회하며 수집

---

## 🔌 API 명세

### 1. 경기 일정 크롤링
KBO 사이트로부터 한 시즌의 경기 정보 데이터를 수집하여 DB에 저장합니다. 이미 저장된 경기 정보라면 새로 수집된 정보로 업데이트합니다. 성공 시 수집된 건수, 저장된 건수, 수정된 건수를 반환합니다.

* **URL**: `POST /api/game-schedule/collect`
* **Request Body**:
```json
{
  "season": 2025,
  "seriesType": "REGULAR_SEASON" 
}
```
* `season`: (필수) 연도 (예: 2025)
* `seriesType`: (선택) `PRESEASON` (시범경기), `REGULAR_SEASON` (정규시즌), `POSTSEASON` (포스트시즌). 생략 시 전체 크롤링. (대소문자 구분 없음)
* **Response Example**:
```json
{
    "code": "OK",
    "message": "성공",
    "data": {
        "collectedCount": 144,
        "savedCount": 140,
        "modifiedCount": 4
    }
}
```

### 2. 일자별 경기 조회
특정 날짜와 팀을 기준으로 경기를 조회합니다. 더블헤더 경기가 있을 수 있으므로 리스트 형태로 반환됩니다.

* **URL**: `GET /api/game-schedule/{team}/{date}`
* **Path Variables**:
  * `team`: (필수) 팀 명 (예: `LOTTE`, `lotte`)
  * `date`: (필수) 경기 날짜 (예: `2025-05-01`)
* **Response Example**:
```json
{
    "code": "OK",
    "message": "성공",
    "data": [
        {
            "gameKey": "20250501-LOTTE-HEROES-1",
            "seriesType": "정규시즌",
            "date": "2025-05-01",
            "time": "18:30:00",
            "homeTeam": "키움 히어로즈",
            "awayTeam": "롯데 자이언츠",
            "homeScore": 0,
            "awayScore": 5,
            "stadium": "고척",
            "relay": ["SPO-T"],
            "gameStatus": "경기 종료",
            "cancellationReason": null
        }
    ]
}
```

### 3. 단일 경기 조회
경기 고유 ID를 이용하여 단일 경기의 정보를 조회합니다.

* **URL**: `GET /api/game-schedule/{gameKey}`
* **Path Variable**:
    * `gameKey`: 경기 고유 ID
* **Response Example**:
```json
{
    "code": "OK",
    "message": "성공",
    "data": {
        "gameKey": "20250501-LOTTE-HEROES-1",
        "seriesType": "정규시즌",
        "date": "2025-05-01",
        "time": "18:30:00",
        "homeTeam": "키움 히어로즈",
        "awayTeam": "롯데 자이언츠",
        "homeScore": 0,
        "awayScore": 5,
        "stadium": "고척",
        "relay": ["SPO-T"],
        "gameStatus": "경기 종료",
        "cancellationReason": null
    }
}
```

---

## 📂 프로젝트 구조

```text
src/main/kotlin/com/colabear754/kbo_scraper
├── api
│   ├── config          # 설정 파일
│   ├── controllers     # API 컨트롤러
│   ├── domain          # 도메인 엔티티
│   ├── dto             # 데이터 전송 객체
│   │   ├── requests    # API 요청 DTO
│   │   ├── responses   # API 응답 DTO
│   │   └── GlobalResponse.kt # 공통 응답 포맷 객체
│   ├── handlers        # 전역 처리 핸들러
│   ├── properties      # 외부 설정 프로퍼티 관리 (@ConfigurationProperties)
│   ├── repositories    # DB 접근 계층
│   ├── scrapers        # KBO 웹사이트 크롤링 및 파싱 로직 (Playwright)
│   └── services        # 비즈니스 로직 (크롤링 흐름 제어, 데이터 조회)
├── initializer         # 현재 시즌 경기 정보 초기화
├── scheduler           # 주기적으로 경기 정보를 수집하기 위한 스케줄러
└── KboScraperApplication.kt # 애플리케이션 실행 클래스
```